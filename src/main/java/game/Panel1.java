package game;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Panel1 extends JPanel {
        private volatile static Server server;
        private BufferedImage background, floor, heart; // 배경 및 바닥 이미지
        private BufferedImage[] countdownImages = new BufferedImage[3];
        private Image hurdle, hurdle_bird;
        private final int DELAY = 30;
        private int map_floorX1; // 맵 이동 좌표
        private int map_background1;
        private int life1 = 3, life2 = 3;
        private long lastCollisionTime = 0;
        private final int MOVE_SPEED = 6; // 이동 속도
        private final int MOVE_SPEED_BACKGROUND = 1;
        private Timer timer = null, mapMovementTimer = null, hurdleCreationTimer = null;
        private JLabel countdownLabel;
        private Image octo1, octo2;
        private int octo1X=100, octo1Y=207, octo2X=100, octo2Y=549;
        private volatile boolean isJumping = false;
        private ImageIcon octo1_slide, octo2_slide, octo1_icon, octo2_icon;
        private ArrayList<Rectangle> hurdles = new ArrayList<>(), collisionAreas = new ArrayList<>(); // 장애물 리스트
        private Random random = new Random();
        private MainFrame frame;

        public Panel1(MainFrame frame) {
                setLayout(null);
                this.frame = frame;

                // 나가기 버튼 추가
                JButton backButton = new JButton("나가기");
                backButton.setBounds(1150, 0, 120, 40);
                backButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                                frame.showPanel("main.java.game.MainPanel");
                                stopServer();
                                mapMovementTimer.stop();
                                hurdleCreationTimer.stop();
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
                octo1_icon = new ImageIcon("src/main/java/image/대기1.gif");
                octo2_icon = new ImageIcon("src/main/java/image/대기2.gif");

                octo1 = octo1_icon.getImage();
                octo2 = octo2_icon.getImage();

                System.out.println("octo1: " + (octo1 == null));
                System.out.println("octo2: " + (octo2 == null));

                // 키 이벤트 처리
                setFocusable(true);
                addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyPressed(KeyEvent e) {
                                if (e.getKeyCode() == KeyEvent.VK_UP && !isJumping) {
                                        startJump();
                                }
                                if (e.getKeyCode() == KeyEvent.VK_DOWN && !isJumping) {
                                        slide();
                                }
                        }

                        @Override
                        public void keyReleased(KeyEvent e) {
                                if (e.getKeyCode() == KeyEvent.VK_DOWN && !isJumping) {
                                        resetSlide();
                                }
                        }
                });


        }

        // 이미지 로드 메서드
        private void loadImages() {
                try {
                        background = ImageIO.read(new File("src/main/java/image/background.png")); // 위쪽 배경
                        floor = ImageIO.read(new File("src/main/java/image/floor.png")); // 바닥
                        heart = ImageIO.read(new File("src/main/java/image/생명.png"));

                        BufferedImage cnt_img3 = ImageIO.read(new File("src/main/java/image/number_3.png"));
                        BufferedImage cnt_img2 = ImageIO.read(new File("src/main/java/image/number_2.png"));
                        BufferedImage cnt_img1 = ImageIO.read(new File("src/main/java/image/number_1.png"));

                        // Resize images (e.g., to 400x400)
                        countdownImages[0] = resizeImage(cnt_img3, 100, 100);
                        countdownImages[1] = resizeImage(cnt_img2, 100, 100);
                        countdownImages[2] = resizeImage(cnt_img1, 100, 100);

                        //캐릭터 슬라이드 이미지
                        octo1_slide = new ImageIcon("src/main/java/image/octo1_slide.png");
                        octo2_slide = new ImageIcon("src/main/java/image/octo2_slide.png");

                        //장애물 이미지
                        hurdle = ImageIO.read(new File("src/main/java/image/선인장.png"));
                        hurdle_bird = new ImageIcon("src/main/java/image/새1.gif").getImage();

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
                mapMovementTimer = new Timer(DELAY, e -> {
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
                                        ((Timer) e.getSource()).stop();
                                        countdownLabel.setVisible(false);
                                        startMapMovementTimer();
                                        startHurdleTimers(); // 장애물 생성 시작
                                }
                        }
                });
                countdownTimer.start();
        }

        // 맵 이동 로직
        private void moveMap() {
                int width = getWidth();

                if(life2<=0){
                        mapMovementTimer.stop();
                        hurdleCreationTimer.stop();
                        new javax.swing.Timer(1000, e -> {
                                stopServer();
                                frame.showPanel("main.java.game.Panel4.win1");
                        }).start();
                }

                // 아래쪽 바닥 좌표를 왼쪽으로 이동
                map_floorX1 -= MOVE_SPEED;
                if (map_floorX1 <= -floor.getWidth()+width) {
                        map_floorX1 = 0;
                }

                // 위쪽 배경 좌표를 왼쪽으로 이동
                map_background1 -= MOVE_SPEED_BACKGROUND;
                if (map_background1 <= -background.getWidth()+width*4) {
                        map_background1 = 0;
                }

                moveHurdles();
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

                int height = getHeight();
                int halfHeight = height / 2;

                // 배경
                g.drawImage(background, map_background1, 0, background.getWidth()/2, height, this);

                // 위쪽 바닥 반복
                g.drawImage(floor, map_floorX1, halfHeight - 60, floor.getWidth(), 60, this);

                // 아래쪽 바닥 반복
                g.drawImage(floor, map_floorX1, height - 60, floor.getWidth(), 60, this);

                for (Rectangle hurdle_ract : hurdles) {
                        if(hurdle_ract.y == 227 || hurdle_ract.y == 569)
                                g.drawImage(hurdle, hurdle_ract.x, hurdle_ract.y, hurdle_ract.width, hurdle_ract.height, this);
                        else
                                g.drawImage(hurdle_bird, hurdle_ract.x, hurdle_ract.y, hurdle_ract.width, hurdle_ract.height, this);
                }

                for(int i=0; i<life1; i++)
                        g.drawImage(heart, i*heart.getWidth() + 30,30, heart.getWidth(), heart.getHeight(), this);

                for(int i=0; i<life2; i++)
                        g.drawImage(heart, i*heart.getWidth() + 30,halfHeight + 30, heart.getWidth(), heart.getHeight(), this);

                // 캐릭터
                g.drawImage(octo1, octo1X, octo1Y, this);
                g.drawImage(octo2, octo2X, octo2Y, this);
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

        // 캐릭터 점프
        private void startJump() {
                isJumping = true; // 점프 상태 활성화
                final int[] characterY = {549};
                int INITIAL_JUMP_VELOCITY = 18;

                Timer jumpTimer = new Timer(DELAY/2, new ActionListener() { // 약 60 FPS
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
                                octo2Y = (int) characterY[0];
                                repaint();

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

        // 캐릭터 슬라이드
        private void slide(){
                octo2 = octo2_slide.getImage();
                octo2Y = 565;
        }

        // 슬라이드 상태 해제
        private void  resetSlide(){
                octo2 = octo2_icon.getImage();
                octo2Y = 549;
        }

        // 장애물 및 충돌 박스 생성 함수
        private void createHurdle() {
                int x = getWidth();
                int width = 65;
                int height = 65;
                int octo1_y, octo2_y;

                if(random.nextInt(3)%3 == 0){
                        octo1_y = 177;
                        octo2_y = 519;
                } else {
                        octo1_y = 227;
                        octo2_y = 569;
                }

                int collisionOffset = 25;

                // 실제 장애물 추가
                hurdles.add(new Rectangle(x, octo2_y, width, height));
                hurdles.add(new Rectangle(x, octo1_y, width, height));
                // 충돌 판정용 영역 설정
                collisionAreas.add(new Rectangle(x + collisionOffset, octo2_y + collisionOffset, width - 2 * collisionOffset, height - 2 * collisionOffset));
                collisionAreas.add(new Rectangle(x + collisionOffset, octo1_y + collisionOffset, width - 2 * collisionOffset, height - 2 * collisionOffset));
        }

        // 장애물 생성 타이머
        private void startHurdleTimers() {
                if (hurdleCreationTimer != null && hurdleCreationTimer.isRunning()) {
                        return;
                }
                // 장애물 생성 타이머 (2~2.6초마다 장애물 생성)
                hurdleCreationTimer = new Timer(getRandomDelay(), e -> {
                        createHurdle();
                        hurdleCreationTimer.stop();
                        startHurdleTimers();
                });
                hurdleCreationTimer.setRepeats(false);
                hurdleCreationTimer.start();
        }

        // 랜덤 초 반환
        private int getRandomDelay() {
                return 2000 + random.nextInt(601);
        }

        // 장애물 이동 및 충돌 처리
        private void moveHurdles() {
                long currentTime = System.currentTimeMillis();

                for (int i = 0; i < hurdles.size(); i++) {
                        Rectangle hurdle = hurdles.get(i);
                        Rectangle collisionArea = collisionAreas.get(i);
                        hurdle.x -= MOVE_SPEED;
                        collisionArea.x -= MOVE_SPEED;

                        // 화면 밖으로 나간 장애물 제거
                        if (hurdle.x + hurdle.width < 0) {
                                hurdles.remove(i);
                                collisionAreas.remove(i);
                                i--; // 리스트 크기가 줄어들기 때문에 인덱스 조정
                                continue;
                        }

                        // 충돌 감지
                        if (checkCollision(collisionArea, octo2X, octo2Y, octo2.getWidth(null), octo2.getHeight(null))) {
                                if (currentTime - lastCollisionTime >= 1000) {
                                        System.out.println("충돌 발생!");
                                        lastCollisionTime = currentTime;
                                        // 충돌 처리 로직 추가
                                        life2 -= 1;
                                }
                        }
                }
        }

        // 충돌 감지
        private boolean checkCollision(Rectangle collisionArea, int octo2X, int octo2Y, int octo2Width, int octo2Height) {
                Rectangle octo2Bounds = new Rectangle(octo2X, octo2Y, octo2Width, octo2Height);
                return collisionArea.intersects(octo2Bounds);
        }
}