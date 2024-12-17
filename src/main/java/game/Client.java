package game;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private AtomicBoolean running;
    private Thread listenerThread;

    private MessageListener messageListener; // 메시지 리스너

    // 메시지 리스너 설정
    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    // 서버에 연결
    public boolean connect(String host, int inputPort) {
        try {
            if(host.isEmpty()) return false;
            socket = new Socket(host, inputPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            running = new AtomicBoolean(true);

            // 서버 메시지를 수신하는 스레드 시작
            listenerThread = new Thread(this::listenToServer);
            listenerThread.start();

            System.out.println("서버에 연결되었습니다.");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 서버로 메시지 전송
    public void sendMessage(String message) {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            out.println(message);
            System.out.println("메시지 전송 완료: " + message);
        } else {
            System.out.println("서버와의 연결이 유효하지 않습니다.");
        }
    }

    // 서버로부터 메시지를 수신
    private void listenToServer() {
        try {
            String message;
            while (running.get() && (message = in.readLine()) != null) {
                System.out.println("서버 메시지: " + message);

                // 리스너에 메시지 전달
                if (messageListener != null) {
                    messageListener.onMessageReceived(message);
                }
            }
        } catch (IOException e) {
            if (running.get()) {
                e.printStackTrace();
            }
        } finally {
            disconnect();
        }
    }

    // 서버와의 연결 종료
    public void disconnect() {
        try {
            running.set(false);

            if (listenerThread != null && listenerThread.isAlive()) {
                listenerThread.interrupt();
            }

            if (in != null) {
                in.close();
            }

            if (out != null) {
                out.close();
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            System.out.println("서버와의 연결이 종료되었습니다.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 연결 상태 확인
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

}