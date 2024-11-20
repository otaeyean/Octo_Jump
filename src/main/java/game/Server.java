package game;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server extends Thread {
    private static final int PORT = 6000;
    private ServerSocket serverSocket = null;  // ServerSocket 객체
    private static Socket guest = null;  // 클라이언트 소켓
    private AtomicBoolean running = new AtomicBoolean(true); // 서버의 연결 상태 관리
    private ClientHandler clientHandler;
    public static volatile boolean connected = false;

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("게임 서버가 시작되었습니다.");

            while (running.get()) {
                try {
                    Socket socket = serverSocket.accept();
                    if (guest == null) {
                        guest = socket;
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
            if (guest != null && !guest.isClosed()) {
                guest.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (clientHandler != null) {
                clientHandler.interrupt();
                guest=null;
            }
            System.out.println("Server.stopServer()");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 클라이언트와의 연결을 처리하는 핸들러 클래스
    static class ClientHandler extends Thread {
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

                String message;
                while (running.get() && (message = in.readLine()) != null) {
                    System.out.println(clientType + " 메시지: " + message);
                    handleGameAction(message);
                }
            } catch (IOException e) {
                if (running.get()) {
                    e.printStackTrace();
                }
            } finally {
                closeConnection();
                guest = null;
                Server.connected = false;
            }
        }

        private void handleGameAction(String message) {
            // 예시: "MOVE 10 20" -> 캐릭터 이동 메시지
            if (message.startsWith("MOVE")) {
                String[] parts = message.split(" ");
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);

                // 상대방에게 새로운 좌표 전달
                sendMessageToClient("MOVE " + x + " " + y);
            }
        }

        private void sendMessageToClient(String message) {
            try {
                PrintWriter targetOut = new PrintWriter(guest.getOutputStream(), true);
                targetOut.println(message);
            } catch (IOException e) {
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
