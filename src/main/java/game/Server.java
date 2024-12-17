package game;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Server extends Thread {
    private static final int PORT = 5000;
    private ServerSocket serverSocket = null;  // ServerSocket 객체
    private static AtomicReference<Socket> guest = new AtomicReference<>(null);  // 클라이언트 소켓
    private AtomicBoolean running = new AtomicBoolean(true); // 서버의 연결 상태 관리
    private ClientHandler clientHandler;
    public static volatile boolean connected = false;

    private MessageListener messageListener; // 메시지 리스너

    // 메시지 리스너 설정
    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    // 클라이언트로 메시지 전송
    public void sendMessageToClient(String message) {
        if (clientHandler != null && guest.get() != null) {
            clientHandler.sendMessageToClient(message);
        } else {
            System.out.println("현재 클라이언트와 연결되어 있지 않습니다.");
        }
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("게임 서버가 시작되었습니다.");

            while (running.get()) {
                try {
                    Socket socket = serverSocket.accept();
                    if (guest.get() == null) {
                        guest.set(socket);
                        System.out.println("Guest가 접속했습니다.");
                        clientHandler = new ClientHandler(socket, "Guest", running);
                        clientHandler.start();
                        connected = true;
                    } else {
                        System.out.println("최대 접속 인원이 초과되어 연결이 거부되었습니다.");
                        socket.close();
                    }
                } catch (SocketException e) {
                    if (!running.get()) {
                        System.out.println("서버가 종료되고 있습니다.");
                    } else {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stopServer();
        }
    }

    // 서버 종료 메서드
    public void stopServer() {
        running.set(false);
        try {
            connected = false;
            Socket guestSocket = guest.getAndSet(null);
            if (guestSocket != null && !guestSocket.isClosed()) {
                // 클라이언트에게 서버 종료 알림
                PrintWriter out = new PrintWriter(guestSocket.getOutputStream(), true);
                out.println("서버가 종료되었습니다.");
                guestSocket.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (clientHandler != null) {
                clientHandler.interrupt();
            }
            System.out.println("Server.stopServer()");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 클라이언트와의 연결을 처리하는 핸들러 클래스
    class ClientHandler extends Thread {
        private Socket socket;
        private String clientType;
        private PrintWriter out;
        private BufferedReader in;
        private AtomicBoolean running;

        public ClientHandler(Socket socket, String clientType, AtomicBoolean running) {
            this.socket = socket;
            this.clientType = clientType;
            this.running = running;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println(clientType + "로 연결되었습니다.");

                String message = null;
                while (running.get() && (message = in.readLine()) != null) {
                    System.out.println(clientType + " 메시지: " + message);

                    // 리스너에 메시지 전달
                    if (messageListener != null) {
                        messageListener.onMessageReceived(message);
                    }
                }

                if (message == null) {
                    System.out.println("클라이언트 연결 종료");
                }
            } catch (IOException e) {
                if (running.get()) {
                    e.printStackTrace();
                }
            } finally {
                closeConnection();
                guest.set(null);
                Server.connected = false;
            }
        }

        public void sendMessageToClient(String message) {
            try {
                if (socket != null && !socket.isClosed()) {
                    out.println(message);
                    System.out.println("메시지 전송 완료: " + message);
                } else {
                    System.out.println("클라이언트 연결이 닫혀 있거나 유효하지 않습니다.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void closeConnection() {
            try {
                if (socket != null && !socket.isClosed()) {
                    out.println("ClientHandler.closeConnection()");
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}