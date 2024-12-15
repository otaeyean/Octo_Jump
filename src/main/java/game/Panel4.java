package game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Panel4 extends JPanel {
    private BufferedImage[] background_images = new BufferedImage[10];
    private BufferedImage[] background_middle = new BufferedImage[30];
    private BufferedImage fooler_block1, fooler_block2, fooler_block3, banner;
    private Image winnerImage;
    private boolean isJumping = false;
    private int winnerX = 600, winnerY = 550;
    private final Random random = new Random();

    public Panel4(MainFrame frame, String winner) {
        // 나가기 버튼
        JButton backButton = new JButton("나가기");
        backButton.setBounds(1150, 0, 120, 40);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.showPanel("main.java.game.MainPanel");
            }
        });
        backButton.setFocusPainted(false);
        add(backButton);

        setLayout(null);
        loadImages(winner);
        setupJumpTimers();
    }

    private void loadImages(String winner) {
        try {
            // 배경 이미지 로딩
            background_images[0] = ImageIO.read(new File("src/main/java/image/배경1.png"));
            background_images[1] = ImageIO.read(new File("src/main/java/image/배경2.png"));
            background_images[2] = ImageIO.read(new File("src/main/java/image/배경_중간1.png"));
            background_images[3] = ImageIO.read(new File("src/main/java/image/배경_중간2.png"));
            background_images[4] = ImageIO.read(new File("src/main/java/image/배경_중간3.png"));
            background_images[5] = ImageIO.read(new File("src/main/java/image/배경_중간4.png"));

            for (int i = 0; i < 22; i++) {
                int idx = i % 4 + 2;
                background_middle[i] = background_images[idx];
            }

            // 바닥 이미지 로딩
            fooler_block1 = ImageIO.read(new File("src/main/java/image/풀_바닥1.png"));
            fooler_block2 = ImageIO.read(new File("src/main/java/image/풀_바닥2.png"));
            fooler_block3 = ImageIO.read(new File("src/main/java/image/풀_바닥3.png"));

            // 승자 이미지 로딩
            if ("octo1".equals(winner)) {
                winnerImage = new ImageIcon("src/main/java/image/octo1.png").getImage();
            } else {
                winnerImage = new ImageIcon("src/main/java/image/octo2.png").getImage();
            }

            banner = ImageIO.read(new File("src/main/java/image/winner.png"));

        } catch (IOException e) {
            System.out.println("이미지 로드 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();

        // 배경 그리기
        for (int x = 0; x < width; x += 60) {
            for (int y = 0; y < height - 300; y += 60) {
                g.drawImage(background_images[0], x, y, 60, 60, this);
            }
        }

        for (int x = 0; x < width; x += 60)
            for (int y = 460; y < height - 60; y += 60)
                g.drawImage(background_images[1], x, y, 60, 60, this);

        for (int x = 0, i = 0; x < width; x += 60) {
            g.drawImage(background_middle[i], x, 420, 60, 60, this);
            i++;
        }

        // 바닥 1~3 그리기
        g.drawImage(fooler_block1, 0, 625, 60, 60, this);
        for (int x = 60; x < width - 60; x += 60) {
            g.drawImage(fooler_block2, x, 625, 60, 60, this);
        }
        g.drawImage(fooler_block3, width - 60, 625, 60, 60, this);

        // 승자 그리기
        g.drawImage(winnerImage, winnerX, winnerY, this);

        // 배너 그리기
        g.drawImage(banner, 300, 100, this);
    }

    private void setupJumpTimers() {
        // 옥토퍼스 1의 점프 타이머
        Timer jumpTimer = new Timer(1000 + random.nextInt(1000), e -> {
            if (!isJumping) startJump(winnerImage, () -> isJumping = false);
        });

        jumpTimer.start();
    }

    private void startJump(Image character, Runnable onJumpComplete) {
        isJumping = true; // 점프 상태 활성화
        final int[] characterY = {549};
        int INITIAL_JUMP_VELOCITY = 18;

        Timer jumpTimer = new Timer(16, new ActionListener() {
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
                winnerY = characterY[0];
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
}
