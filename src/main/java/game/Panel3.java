package game;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Panel3 extends JPanel {

        private BufferedImage background1, background2, ground; // 배경 및 바닥 이미지
        private BufferedImage[] background_middle = new BufferedImage[30]; // 중간 배경 배열
        private int map_floorX1, map_floorX2; // 맵 이동 좌표
        private int map_background1, map_background2;
        private final int MOVE_SPEED = 3; // 이동 속도
        private final int MOVE_SPEED_BACKGROUND = 1;

        public Panel3(MainFrame frame) {
                setLayout(null);

                // 나가기 버튼 추가
                JButton backButton = new JButton("나가기");
                backButton.setBounds(1150, 0, 120, 40);
                backButton.addActionListener(e -> frame.showPanel("main.java.game.MainPanel"));
                add(backButton);

                loadImages(); // 이미지 로드

                // map_floorX2와 map_background2의 초기 위치 설정
                map_floorX2 = 1266;
                map_background2 = 1266;

                // 타이머로 맵 이동 처리
                Timer timer = new Timer(30, e -> {
                        moveMap();
                        repaint();
                });
                timer.start();
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
}
