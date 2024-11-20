package game;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Panel1 extends JPanel {
        private volatile static Server server;
        private BufferedImage background1, background2, ground; // 배경 및 바닥 이미지
        private BufferedImage[] background_middle = new BufferedImage[30]; // 중간 배경 배열
        private BufferedImage[] countdownImages = new BufferedImage[3];
        private int map_floorX1, map_floorX2; // 맵 이동 좌표
        private int map_background1, map_background2;
        private final int MOVE_SPEED = 3; // 이동 속도
        private final int MOVE_SPEED_BACKGROUND = 1;
        private Timer timer = null;
        private Timer mapMovementTimer = null;
        private JLabel countdownLabel;
        private JLabel octo1, octo2;
        private boolean isJumping = false;

        public Panel1(MainFrame frame) {
                setLayout(null);

                // 나가기 버튼 추가
                JButton backButton = new JButton("나가기");
                backButton.setBounds(1150, 0, 120, 40);
                backButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                                frame.showPanel("main.java.game.MainPanel");
                                stopServer();

                        }
                });
                backButton.setFocusPainted(false);
                add(backButton);

                // 다른 플레이어 대기 표시 레이블
                JLabel ready = new JLabel("다른 플레이어를 기다리는 중");
                ready.setFont(new Font("굴림", Font.BOLD, 50));
                ready.setForeground(Color.BLACK);
                ready.setBounds(320, 400, 1000, 100);
                add(ready);

                // 게임 시작 전 카운트 다운 표시 레이블
                countdownLabel = new JLabel();
                countdownLabel.setBounds(400, 200, 466, 500); // Adjust size and position as needed
                countdownLabel.setHorizontalAlignment(SwingConstants.CENTER);
                countdownLabel.setVisible(false);
                add(countdownLabel);

                // 다른 플레이어 대기 중 표시 타이머
                final int[] dotCount = new int[1];
                timer = new Timer(1000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                                if (Server.connected) {
                                        timer.stop();
                                        ready.setVisible(false);
                                        startCountdown();
                                        return;
                                }

                                // Update the message every second
                                dotCount[0] = (dotCount[0] + 1) % 4;  // Loop through 0 to 3
                                String dots = ".".repeat(dotCount[0]);
                                ready.setText("다른 플레이어를 기다리는 중" + dots);
                        }
                });
                timer.start();

                loadImages(); // 이미지 로드

                // 캐릭터1, 캐릭터2 표시
                ImageIcon octo1_icon = new ImageIcon("src/main/java/image/대기1.gif");
                ImageIcon octo2_icon = new ImageIcon("src/main/java/image/대기2.gif");

                octo1 = new JLabel(octo1_icon);
                octo2 = new JLabel(octo2_icon);
                octo1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 상단에 10픽셀 여백 추가
                octo2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                octo1.setBounds(100, 207, octo1_icon.getIconWidth(), octo1_icon.getIconHeight());
                octo2.setBounds(100, 548, octo2_icon.getIconWidth(), octo2_icon.getIconHeight());

                add(octo1);
                add(octo2);

                setFocusable(true);
                addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyPressed(KeyEvent e) {
                                if (e.getKeyCode() == KeyEvent.VK_UP && !isJumping) {
                                        startJump();
                                }
                        }
                });

        }

        // 이미지 로드 메서드
        private void loadImages() {
                try {
                        background1 = ImageIO.read(new File("src/main/java/image/배경1.png")); // 위쪽 배경
                        background2 = ImageIO.read(new File("src/main/java/image/배경2.png")); // 아래쪽 배경
                        ground = ImageIO.read(new File("src/main/java/image/풀_바닥2.png")); // 바닥
                        // 중간 배경 이미지 배열 초기화
                        BufferedImage middleImage1 = ImageIO.read(new File("src/main/java/image/배경_중간1.png"));
                        BufferedImage middleImage2 = ImageIO.read(new File("src/main/java/image/배경_중간2.png"));
                        BufferedImage middleImage3 = ImageIO.read(new File("src/main/java/image/배경_중간3.png"));
                        BufferedImage middleImage4 = ImageIO.read(new File("src/main/java/image/배경_중간4.png"));

                        BufferedImage img3 = ImageIO.read(new File("src/main/java/image/number_3.png"));
                        BufferedImage img2 = ImageIO.read(new File("src/main/java/image/number_2.png"));
                        BufferedImage img1 = ImageIO.read(new File("src/main/java/image/number_1.png"));

                        // Resize images (e.g., to 400x400)
                        countdownImages[0] = resizeImage(img3, 100, 100);
                        countdownImages[1] = resizeImage(img2, 100, 100);
                        countdownImages[2] = resizeImage(img1, 100, 100);

                        // map_floorX2와 map_background2의 초기 위치 설정
                        map_floorX2 = 1266;
                        map_background2 = 1266;

                        for (int i = 0; i < background_middle.length; i++) {
                                background_middle[i] = switch (i % 4) {
                                        case 0 -> middleImage1;
                                        case 1 -> middleImage2;
                                        case 2 -> middleImage3;
                                        default -> middleImage4;
                                };
                        }
                } catch (IOException e) {
                        System.out.println("이미지 로드 오류: " + e.getMessage());
                        e.printStackTrace();
                }
        }

        // 맵 이동 타이머
        private void startMapMovementTimer() {
                // 맵 이동 타이머가 이미 실행 중이라면 다시 시작하지 않도록 방지
                if (mapMovementTimer != null && mapMovementTimer.isRunning()) {
                        return;
                }

                // 타이머로 맵 이동 처리
                mapMovementTimer = new Timer(16, e -> {
                        moveMap();
                        repaint();
                });
                mapMovementTimer.start();
        }

        // 카운트 다운 시작 타이머
        private void startCountdown() {
                countdownLabel.setVisible(true);
                final int[] countdownIndex = {0};

                Timer countdownTimer = new Timer(1000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                                if (countdownIndex[0] < countdownImages.length) {
                                        countdownLabel.setIcon(new ImageIcon(countdownImages[countdownIndex[0]]));
                                        countdownIndex[0]++;
                                } else {
                                        ((Timer) e.getSource()).stop(); // Stop the countdown timer
                                        countdownLabel.setVisible(false); // Hide the countdown label
                                        startMapMovementTimer(); // Start the map movement timer
                                }
                        }
                });
                countdownTimer.start();
        }

        // 맵 이동 로직
        private void moveMap() {
                int width = getWidth();

                // 아래쪽 바닥 좌표를 왼쪽으로 이동
                map_floorX1 -= MOVE_SPEED;
                map_floorX2 -= MOVE_SPEED;
                if (map_floorX1 <= -width) {
                        map_floorX1 = map_floorX2 + width;
                }
                if (map_floorX2 <= -width) {
                        map_floorX2 = map_floorX1 + width;
                }

                // 위쪽 배경 좌표를 왼쪽으로 이동
                map_background1 -= MOVE_SPEED_BACKGROUND;
                map_background2 -= MOVE_SPEED_BACKGROUND;
                if (map_background1 <= -width) {
                        map_background1 = map_background2 + width;
                }
                if (map_background2 <= -width) {
                        map_background2 = map_background1 + width;
                }
        }

        // 서버 시작
        public static synchronized void startServer() {
                // 기존 서버가 실행 중이면 종료하고 새로 시작
                if (server != null) {
                        System.out.println("서버 종료 중...");
                        server.stopServer();  // 기존 서버 종료
                }

                // 새 서버 객체 생성 후 실행
                server = new Server();
                server.start();  // 서버 시작
        }

        // 서버 종료
        private void stopServer() {
                server.stopServer();
                server.interrupt();
                server = null;
        }

        @Override
        protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                int width = getWidth();
                int height = getHeight();
                int halfHeight = height / 2;
                int quarterHeight = height / 4;

                // 위쪽 화면 (상단 배경1)
                g.drawImage(background1, map_background1, 0, width, quarterHeight, this);
                g.drawImage(background1, map_background2, 0, width, quarterHeight, this);

                // 중간 배경 반복
                for (int i = 0, x = map_background1; x < width; x += 60, i++) {
                        g.drawImage(background_middle[i % background_middle.length], x, quarterHeight, 60, 60, this);
                }
                for (int i = 0, x = map_background2; x < width; x += 60, i++) {
                        g.drawImage(background_middle[i % background_middle.length], x, quarterHeight, 60, 60, this);
                }

                // 위쪽 절반 하단 배경2
                g.drawImage(background2, map_background1, quarterHeight + 60, width, quarterHeight - 60, this);
                g.drawImage(background2, map_background2, quarterHeight + 60, width, quarterHeight - 60, this);

                // 위쪽 바닥 반복
                for (int x = map_floorX1; x < width + 60; x += 60) {
                        g.drawImage(ground, x, halfHeight - 60, 60, 60, this);
                }
                for (int x = map_floorX2; x < width + 60; x += 60) {
                        g.drawImage(ground, x, halfHeight - 60, 60, 60, this);
                }

                // 아래쪽 화면 (상단 배경1)
                g.drawImage(background1, map_background1, halfHeight, width, quarterHeight, this);
                g.drawImage(background1, map_background2, halfHeight, width, quarterHeight, this);

                // 아래쪽 중간 배경 반복
                for (int i = 0, x = map_background1; x < width; x += 60, i++) {
                        g.drawImage(background_middle[i % background_middle.length], x, halfHeight + quarterHeight, 60, 60, this);
                }
                for (int i = 0, x = map_background2; x < width; x += 60, i++) {
                        g.drawImage(background_middle[i % background_middle.length], x, halfHeight + quarterHeight, 60, 60, this);
                }

                // 아래쪽 절반 하단 배경2
                g.drawImage(background2, map_background1, halfHeight + quarterHeight + 60, width, quarterHeight - 60, this);
                g.drawImage(background2, map_background2, halfHeight + quarterHeight + 60, width, quarterHeight - 60, this);

                // 아래쪽 바닥 반복
                for (int x = map_floorX1; x < width + 60; x += 60) {
                        g.drawImage(ground, x, height - 60, 60, 60, this);
                }
                for (int x = map_floorX2; x < width + 60; x += 60) {
                        g.drawImage(ground, x, height - 60, 60, 60, this);
                }
        }

        // 이미지 크기 변경 메서드
        private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
                Image tmp = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = resized.createGraphics();
                g2d.drawImage(tmp, 0, 0, null);
                g2d.dispose();
                return resized;
        }

        private void startJump() {
                isJumping = true; // 점프 상태 활성화
                final int[] characterY = {548};
                int INITIAL_JUMP_VELOCITY = 18;

                Timer jumpTimer = new Timer(16, new ActionListener() { // 약 60 FPS
                        private int velocity = -INITIAL_JUMP_VELOCITY; // 초기 점프 속도 (위로)
                        private int gravity = 1; // 중력 가속도
                        private int initialY = characterY[0]; // 초기 Y 위치

                        @Override
                        public void actionPerformed(ActionEvent e) {
                                // 새로운 위치 계산
                                characterY[0] += velocity;

                                // 속도 업데이트 (중력 적용)
                                velocity += gravity;

                                // 캐릭터 위치 갱신
                                octo2.setLocation(octo2.getX(), (int) characterY[0]);

                                // 바닥에 도달했는지 확인
                                if (characterY[0] >= initialY) {
                                        characterY[0] = initialY; // 바닥 위치로 조정
                                        ((Timer) e.getSource()).stop(); // 타이머 중지
                                        isJumping = false; // 점프 상태 해제
                                }
                        }
                });
                jumpTimer.start();
        }
}
