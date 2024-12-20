package game;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Panel3 extends JPanel {
        private BufferedImage background, floor, heart, cloud; // 배경 및 바닥 이미지
        private BufferedImage[] ItemImages = new BufferedImage[5];
        private BufferedImage[] countdownImages = new BufferedImage[3];
        private Image hurdle, hurdle_bird;
        private final int DELAY = 30;
        private int map_floorX1; // 맵 이동 좌표
        private int map_background1;
        private int life1 = 3, life2 = 3;
        private long lastCollisionTime = 0;
        private final int MOVE_SPEED = 4; // 이동 속도
        private final int MOVE_SPEED_BACKGROUND = 1;
        private Timer timer = null, connect = null;
        private JLabel countdownLabel;
        private Image octo1, octo2;
        private int octo1X=100, octo1Y=207, octo2X=100, octo2Y=549;
        private volatile boolean isJumping = false, isSlide = false;
        private ImageIcon octo1_slide, octo2_slide, octo1_icon, octo2_icon;
        private CopyOnWriteArrayList<Rectangle> hurdles = new CopyOnWriteArrayList<>(), collisionAreas = new CopyOnWriteArrayList<>(), collisionItems = new CopyOnWriteArrayList<>();
        private CopyOnWriteArrayList<Item> items = new CopyOnWriteArrayList<>();
        private MainFrame frame;
        private Client client;
        private SoundPlayer bgm, jumpEffect, slideEffect, damageEffect, itemEffect;
        private boolean wasConnected = false;
        private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        private ScheduledFuture<?> jumpFutureHost = null, jumpFutureGuest = null;
        private boolean cloudGuest, cloudHost, itemJumpHost, itemJumpGuest, guardGuest, guardHost;

        public Panel3(MainFrame frame, Client client) {
                setLayout(null);
                this.frame = frame;
                this.client = client;

                // 나가기 버튼 추가
                JButton backButton = new JButton("나가기");
                backButton.setBounds(1150, 0, 120, 40);
                backButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                                frame.showPanel("main.java.game.MainPanel");
                                stopGameLoop();
                                bgm.stop();
                        }
                });
                backButton.setFocusPainted(false);
                add(backButton);

                client.setMessageListener(message -> {
                    switch (message) {
                        case "jump" -> JumpOther();
                        case "slideOn" -> slideOther();
                        case "slideOff" -> resetSlideOther();
                        case "hurdle Bird" -> createHurdle("hurdle Bird");
                        case "hurdle Cactus" -> createHurdle("hurdle Cactus");
                        case "guest win" -> win("guest");
                        case "host win" -> win("host");
                        default -> {
                                    if(message.startsWith("life2")) {
                                            String[] split = message.split(" ");
                                            life2 = Integer.parseInt(split[1]);
                                            damageEffect.play(false);
                                    }
                                    else if(message.startsWith("item")) {
                                            String[] split = message.split(" ");
                                            if(split[1].equals("host")){
                                                    createItem(split[1], Integer.parseInt(split[2]));
                                            } else if (split[1].equals("guest")){
                                                    createItem(split[1], Integer.parseInt(split[2]));
                                            } else if(split[1].equals("use")){
                                                    switch (Integer.parseInt(split[2])){
                                                            case 0 -> { // 상대 시야 방해
                                                                    cloudGuest = true;
                                                                    scheduler.schedule(() -> {
                                                                            cloudGuest = false;
                                                                    }, 5000, TimeUnit.MILLISECONDS);
                                                            }
                                                            case 1 -> { // 상대 점프력증가
                                                                    itemJumpGuest = true;
                                                                    scheduler.schedule(() -> {
                                                                            itemJumpGuest = false;
                                                                    }, 5000, TimeUnit.MILLISECONDS);
                                                            }
                                                            case 2 -> {} // 체력 회복
                                                            case 3 -> { // 무적 5초
                                                                    guardHost = true;
                                                                    scheduler.schedule(() -> {
                                                                            guardHost = false;
                                                                    }, 5000, TimeUnit.MILLISECONDS);
                                                            }
                                                            default -> {}
                                                    }
                                            }
                                    }
                        }
                    }
                });

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

                // 연결 체크
                connect = new Timer(DELAY, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                                if (client.isConnected()) {
                                        if (!wasConnected) { // 처음 연결된 경우
                                                wasConnected = true; // 상태를 업데이트
                                                ready.setVisible(false);
                                                startCountdown();
                                                System.out.println("연결되었습니다.");
                                        }
                                } else {
                                        if (wasConnected && life1!=0 && life2!=0) { // 연결이 끊어진 경우
                                                JOptionPane.showMessageDialog(
                                                        null,
                                                        "서버와 연결이 끊어졌습니다.",
                                                        "연결 실패",
                                                        JOptionPane.ERROR_MESSAGE
                                                );
                                                wasConnected = false;
                                                stopGameLoop();
                                                bgm.stop();
                                                frame.showPanel("main.java.game.MainPanel");
                                        }
                                }
                        }
                });
                connect.start();

                // 다른 플레이어 대기 중 표시 타이머
                final int[] dotCount = new int[1];
                timer = new Timer(1000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
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

                bgm = new SoundPlayer("src/main/java/sound/게임시작.wav", -20.0f);
                if(!bgm.isPlaying()){
                        bgm.play(true);
                }
                jumpEffect = new SoundPlayer("src/main/java/sound/점프.wav", -20.0f);
                slideEffect = new SoundPlayer("src/main/java/sound/슬라이드.wav", -20.0f);
                itemEffect = new SoundPlayer("src/main/java/sound/아이템.wav", -20.0f);
                damageEffect = new SoundPlayer("src/main/java/sound/충돌.wav", -20.0f);

                setFocusable(true);
                requestFocusInWindow();
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

                        ItemImages[0] = ImageIO.read(new File("src/main/java/image/상대시야방해.png"));
                        ItemImages[1] = ImageIO.read(new File("src/main/java/image/점프력증가.png"));
                        ItemImages[2] = ImageIO.read(new File("src/main/java/image/체력회복.png"));
                        ItemImages[3] = ImageIO.read(new File("src/main/java/image/무적5초.png"));

                        cloud = ImageIO.read(new File("src/main/java/image/구름.png"));

                } catch (IOException e) {
                        System.out.println("이미지 로드 오류: " + e.getMessage());
                        e.printStackTrace();
                }
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
                                        startGameLoop();
                                }
                        }
                });
                countdownTimer.start();
        }

        private void startGameLoop() {
                scheduler = Executors.newScheduledThreadPool(1); // 장애물 생성 및 다른 작업
                long tickInterval = 1000 / 60; // 60 FPS 기준

                // 맵 이동 및 렌더링
                scheduler.scheduleAtFixedRate(() -> {
                        moveMap();
                        repaint();
                }, 0, tickInterval, TimeUnit.MILLISECONDS);
        }

        private void stopGameLoop() {
                if (scheduler != null && !scheduler.isShutdown()) {
                        scheduler.shutdown();
                }
        }

        // 맵 이동 로직
        private void moveMap() {
                int width = getWidth();

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
                checkItemCollisions();
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

                for (Item item_rect : items) {
                        g.drawImage(ItemImages[item_rect.type], item_rect.bounds.x, item_rect.bounds.y, item_rect.bounds.width, item_rect.bounds.height, this);
                }

                for(int i=0; i<life1; i++)
                        g.drawImage(heart, i*heart.getWidth() + 30,30, heart.getWidth(), heart.getHeight(), this);

                for(int i=0; i<life2; i++)
                        g.drawImage(heart, i*heart.getWidth() + 30,halfHeight + 30, heart.getWidth(), heart.getHeight(), this);

                // 캐릭터
                g.drawImage(octo1, octo1X, octo1Y, this);
                g.drawImage(octo2, octo2X, octo2Y, this);

                // 시야가리기 구름
                if(cloudGuest){
                        g.drawImage(cloud, 300, 110, this);
                        g.drawImage(cloud, 500, 110, this);
                        g.drawImage(cloud, 700, 110, this);
                }
                if(cloudHost){
                        g.drawImage(cloud, 300, 450, this);
                        g.drawImage(cloud, 500, 450, this);
                        g.drawImage(cloud, 700, 450, this);
                }

                // 무적효과
                if(guardGuest)
                        g.drawImage(ItemImages[3], octo1X+13, octo1Y+10, this);
                if(guardHost)
                        g.drawImage(ItemImages[3], octo2X+13, octo2Y+10, this);
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
                if (scheduler.isShutdown()) return;
                isJumping = true; // 점프 상태 활성화
                final int[] characterY = {207};
                int INITIAL_JUMP_VELOCITY;
                if(itemJumpGuest) INITIAL_JUMP_VELOCITY = 22;
                else INITIAL_JUMP_VELOCITY = 18;
                jumpEffect.play(false);
                client.sendMessage("jump");

                Runnable jumpTask = new Runnable() {
                        private int velocity = -INITIAL_JUMP_VELOCITY; // 초기 점프 속도 (위로)
                        private int gravity = 1; // 중력 가속도
                        private int initialY = characterY[0]; // 초기 Y 위치

                        @Override
                        public void run() {
                                // 새로운 위치 계산
                                characterY[0] += velocity;

                                // 속도 업데이트 (중력 적용)
                                velocity += gravity;

                                // 캐릭터 위치 갱신
                                octo1Y = (int) characterY[0];
                                repaint();

                                // 바닥에 도달했는지 확인
                                if (characterY[0] >= initialY) {
                                        characterY[0] = initialY; // 바닥 위치로 조정
                                        if (jumpFutureGuest != null) {
                                                jumpFutureGuest.cancel(false); // 점프 작업만 취소
                                        }
                                        isJumping = false; // 점프 상태 해제
                                }
                        }
                };

                // 점프 타이머 시작 (반복 실행)
                jumpFutureGuest = scheduler.scheduleAtFixedRate(jumpTask, 0, DELAY / 2, TimeUnit.MILLISECONDS);
        }

        // 캐릭터 슬라이드
        private void slide(){
                octo1 = octo1_slide.getImage();
                octo1Y = 223;
                if(!isSlide){
                        isSlide = true;
                        client.sendMessage("slideOn");
                        slideEffect.play(false);
                }
        }

        // 슬라이드 상태 해제
        private void  resetSlide(){
                octo1 = octo1_icon.getImage();
                octo1Y = 207;
                isSlide = false;
                client.sendMessage("slideOff");
        }

        // 상대 캐릭터 점프
        private void JumpOther() {
                if (scheduler.isShutdown()) return;
                final int[] characterY = {549};
                int INITIAL_JUMP_VELOCITY;
                if(itemJumpHost) INITIAL_JUMP_VELOCITY = 22;
                else INITIAL_JUMP_VELOCITY = 18;
                jumpEffect.play(false);

                Runnable jumpTask = new Runnable() {
                        private int velocity = -INITIAL_JUMP_VELOCITY; // 초기 점프 속도 (위로)
                        private int gravity = 1; // 중력 가속도
                        private int initialY = characterY[0]; // 초기 Y 위치

                        @Override
                        public void run() {
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
                                        if (jumpFutureHost != null) {
                                                jumpFutureHost.cancel(false); // 점프 작업만 취소
                                        }
                                }
                        }
                };

                // 점프 타이머 시작 (반복 실행)
                jumpFutureHost = scheduler.scheduleAtFixedRate(jumpTask, 0, DELAY / 2, TimeUnit.MILLISECONDS);
        }

        // 상대 캐릭터 슬라이드
        private void slideOther(){
                octo2 = octo2_slide.getImage();
                octo2Y = 565;
                slideEffect.play(false);
        }

        // 상대 슬라이드 상태 해제
        private void  resetSlideOther(){
                octo2 = octo2_icon.getImage();
                octo2Y = 549;
        }

        // 장애물 및 충돌 박스 생성 함수
        private void createHurdle(String type) {
                int x = getWidth();
                int width = 65;
                int height = 65;
                int octo1_y, octo2_y;

                if(type.equals("hurdle Bird")){
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

        // 아이템 생성 및 처리 함수
        private void createItem(String player, int itemType) {
                int x = getWidth();
                int width = 65;
                int height = 65;
                int item1_y = 147; // 캐릭터 1 아이템 높이
                int item2_y = 489; // 캐릭터 2 아이템 높이
                int collisionOffset = 25;

                if(player.equals("guest")){
                        items.add(new Item(new Rectangle(x, item1_y, width, height), itemType));
                        // 충돌 판정용 영역 설정
                        collisionItems.add(new Rectangle(x + collisionOffset, item1_y + collisionOffset,
                                width - 2 * collisionOffset, height - 2 * collisionOffset));
                } else if(player.equals("host")){
                        items.add(new Item(new Rectangle(x, item2_y, width, height), itemType));
                        collisionItems.add(new Rectangle(x + collisionOffset, item2_y + collisionOffset,
                                width - 2 * collisionOffset, height - 2 * collisionOffset));
                }
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
                        if (checkCollision(collisionArea, octo1X, octo1Y, octo1.getWidth(null), octo1.getHeight(null))) {
                                if (currentTime - lastCollisionTime >= 1000 && !guardGuest) {
                                        lastCollisionTime = currentTime;
                                        life1 -= 1;
                                        client.sendMessage("life1 " + life1);
                                }
                        }
                }
        }

        // 충돌 감지
        private boolean checkCollision(Rectangle collisionArea, int octo1X, int octo1Y, int octo1Width, int octo1Height) {
                Rectangle octo2Bounds = new Rectangle(octo1X, octo1Y, octo1Width, octo1Height);
                return collisionArea.intersects(octo2Bounds);
        }

        private void win(String winner) {
                stopGameLoop();
                if(winner.equals("guest")){
                        new javax.swing.Timer(1000, e -> {
                                frame.showPanel("main.java.game.Panel4.win1");
                                bgm.stop();
                        }) {{
                                setRepeats(false);
                                start();
                        }};
                } else if(winner.equals("host")){
                        new javax.swing.Timer(1000, e -> {
                                frame.showPanel("main.java.game.Panel4.win2");
                                bgm.stop();
                        }) {{
                                setRepeats(false);
                                start();
                        }};
                }
        }

        // 아이템 충돌 감지
        private void checkItemCollisions() {
                for (int i = 0; i < items.size(); i++) {
                        Item item = items.get(i);
                        Rectangle collisionArea = collisionItems.get(i);

                        item.bounds.x -= MOVE_SPEED;
                        collisionArea.x -= MOVE_SPEED;

                        // 화면 밖으로 나간 장애물 제거
                        if (item.bounds.x + item.bounds.width < 0) {
                                removeItem(i);
                                i--;
                                continue;
                        }

                        // 캐릭터 1 충돌 확인
                        if (checkCollision(collisionArea, octo1X, octo1Y, octo1.getWidth(null), octo1.getHeight(null))) {
                                itemEffect.play(false);
                                applyItemEffect(item.type);
                                removeItem(i); // 아이템 제거
                                i--; // 리스트 크기 줄어듦에 따른 인덱스 조정
                        }

                        // 캐릭터 2 충돌 확인
                        if (checkCollision(collisionArea, octo2X, octo2Y, octo2.getWidth(null), octo2.getHeight(null))) {
                                itemEffect.play(false);
                                removeItem(i); // 아이템 제거
                                i--; // 리스트 크기 줄어듦에 따른 인덱스 조정
                        }
                }
        }

        // 아이템 적용
        private void applyItemEffect(int itemType) {
                switch (itemType) {
                        case 0 -> { // 상대 시야 방해
                                cloudHost = true;
                                client.sendMessage("item use " + itemType);
                                scheduler.schedule(() -> {
                                        cloudHost = false;
                                }, 5000, TimeUnit.MILLISECONDS);
                        }
                        case 1 -> { // 상대 점프력증가
                                client.sendMessage("item use " + itemType);
                                itemJumpHost = true;
                                scheduler.schedule(() -> {
                                        itemJumpHost = false;
                                }, 5000, TimeUnit.MILLISECONDS);
                        }
                        case 2 -> { // 체력 회복
                                life1 += 1;
                                client.sendMessage("life1 " + life1);
                        }
                        case 3 -> { // 무적 5초
                                client.sendMessage("item use " + itemType);
                                guardGuest = true;
                                scheduler.schedule(() -> {
                                        guardGuest = false;
                                }, 5000, TimeUnit.MILLISECONDS);
                        }
                        default -> System.out.println("Unknown item type: " + itemType);
                }
        }

        private void removeItem(int index) {
                items.remove(index);
                collisionItems.remove(index);
        }
}