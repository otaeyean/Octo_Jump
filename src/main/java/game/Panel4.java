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
    private BufferedImage fooler_block1, fooler_block2, fooler_block3;
    private Image winnerImage;
    private int winnerX = 640, winnerY = 550;

    public Panel4(MainFrame frame, String winner) {
        setLayout(null);
        loadImages(winner);
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
    }

    public void startWinnerJump() {
        final int groundY = 550; // Ground level Y position
        final int jumpHeight = 150; // Maximum jump height
        final int jumpSpeed = 5; // Speed of jump movement
        Random random = new Random();

        Timer jumpTimer = new Timer(30, new ActionListener() {
            private int velocity = -jumpSpeed;
            private boolean jumping = true;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (jumping) {
                    winnerY += velocity;

                    // Check if reached max height
                    if (winnerY <= groundY - jumpHeight) {
                        velocity = jumpSpeed; // Start falling
                    }
                } else {
                    winnerY += velocity;

                    // Check if reached ground
                    if (winnerY >= groundY) {
                        winnerY = groundY;
                        velocity = -jumpSpeed;
                        jumping = true;

                        // Random delay before next jump
                        ((Timer) e.getSource()).setDelay(random.nextInt(2000) + 500);
                        return;
                    }
                }

                jumping = winnerY < groundY;
                repaint();
            }
        });

        jumpTimer.setInitialDelay(random.nextInt(2000) + 500); // Initial random delay
        jumpTimer.start();
    }
}
