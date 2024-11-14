package main.java.game;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Panel1 extends JPanel {

        private BufferedImage background1, background2, ground; // ��� �� �ٴ� �̹���
        private BufferedImage[] background_middle = new BufferedImage[30]; // �߰� ��� �迭
        private int mapX = 0; // �� �̵� ��ǥ
        private final int MOVE_SPEED = 5; // �̵� �ӵ�

        public Panel1(MainFrame frame) {
                setLayout(null);

                // ������ ��ư �߰�
                JButton backButton = new JButton("������");
                backButton.setBounds(1150, 0, 120, 40);
                backButton.addActionListener(e -> frame.showPanel("main.java.game.MainPanel"));
                add(backButton);

                loadImages(); // �̹��� �ε�

                // Ÿ�̸ӷ� �� �̵� ó��
                Timer timer = new Timer(15, e -> {
                        moveMap();
                        repaint();
                });
                timer.start();
        }

        // �̹��� �ε� �޼���
        private void loadImages() {
                try {
                        background1 = ImageIO.read(new File("src/main/java/image/���1.png")); // ���� ���
                        background2 = ImageIO.read(new File("src/main/java/image/���2.png")); // �Ʒ��� ���
                        ground = ImageIO.read(new File("src/main/java/image/Ǯ_�ٴ�2.png")); // �ٴ�
                        // �߰� ��� �̹��� �迭 �ʱ�ȭ
                        BufferedImage middleImage1 = ImageIO.read(new File("src/main/java/image/���_�߰�1.png"));
                        BufferedImage middleImage2 = ImageIO.read(new File("src/main/java/image/���_�߰�2.png"));
                        BufferedImage middleImage3 = ImageIO.read(new File("src/main/java/image/���_�߰�3.png"));
                        BufferedImage middleImage4 = ImageIO.read(new File("src/main/java/image/���_�߰�4.png"));

                        for (int i = 0; i < background_middle.length; i++) {
                                background_middle[i] = switch (i % 4) {
                                        case 0 -> middleImage1;
                                        case 1 -> middleImage2;
                                        case 2 -> middleImage3;
                                        default -> middleImage4;
                                };
                        }
                } catch (IOException e) {
                        System.out.println("�̹��� �ε� ����: " + e.getMessage());
                        e.printStackTrace();
                }
        }

        // �� �̵� ����
        private void moveMap() {
                mapX -= MOVE_SPEED; // ���� �������� �̵�
                if (mapX <= -getWidth()) {
                        mapX = 0; // ȭ�� ���� �����ϸ� �ʱ�ȭ
                }
        }

        @Override
        protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                int width = getWidth();
                int height = getHeight();
                int halfHeight = height / 2;
                int quarterHeight = height / 4;

                // ���� ȭ��
                g.drawImage(background1, mapX, 0, width, quarterHeight, this); // ���� ���� ��� ���1
                g.drawImage(background1, mapX + width, 0, width, quarterHeight, this);
                for (int i = 0, x = mapX; x < width; x += 60, i++) { // �߰� ���
                        g.drawImage(background_middle[i % background_middle.length], x, quarterHeight, 60, 60, this);
                }
                g.drawImage(background2, mapX, quarterHeight + 60, width, quarterHeight - 60, this); // ���� ���� �ϴ� ���2
                g.drawImage(background2, mapX + width, quarterHeight + 60, width, quarterHeight - 60, this);
                for (int x = mapX; x < width + 60; x += 60) { // ���� �ٴ�
                        g.drawImage(ground, x, halfHeight - 60, 60, 60, this);
                }

                // �Ʒ��� ȭ��
                g.drawImage(background1, mapX, halfHeight, width, quarterHeight, this); // �Ʒ��� ���� ��� ���1
                g.drawImage(background1, mapX + width, halfHeight, width, quarterHeight, this);
                for (int i = 0, x = mapX; x < width; x += 60, i++) { // �߰� ���
                        g.drawImage(background_middle[i % background_middle.length], x, halfHeight + quarterHeight, 60, 60, this);
                }
                g.drawImage(background2, mapX, halfHeight + quarterHeight + 60, width, quarterHeight - 60, this); // �Ʒ��� ���� �ϴ� ���2
                g.drawImage(background2, mapX + width, halfHeight + quarterHeight + 60, width, quarterHeight - 60, this);
                for (int x = mapX; x < width + 60; x += 60) { // �Ʒ��� �ٴ�
                        g.drawImage(ground, x, height - 60, 60, 60, this);
                }
        }
}
