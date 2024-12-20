package game;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class MainPanel extends JPanel {
    private BufferedImage[] background_images = new BufferedImage[10];
    private BufferedImage[] background_middle = new BufferedImage[30];
    private BufferedImage fooler_block1, fooler_block2, fooler_block3, logo;
    private JLabel octo1, octo2;

    private int Octo1_imageX = 0;
    private int Octo2_imageX = 100;
    private int Octo1_speed = 4;
    private int Octo2_speed = 3;

    private SoundPlayer bgm;

    public MainPanel(MainFrame frame) {
        setLayout(null);

        JButton createRoomButton = new JButton("방 만들기");
        JButton joinRoomButton = new JButton("참여하기");

        createRoomButton.setBounds(580, 300, 120, 40); // 중앙에 위치
        joinRoomButton.setBounds(580, 360, 120, 40);

        // 방 만들기 버튼 클릭
        createRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.showPanel("main.java.game.Panel1");
                bgm.stop();
            }
        });
        // 참여 버튼 클릭
        joinRoomButton.addActionListener(e -> {
            frame.showPanel("main.java.game.Panel2");
            bgm.stop();
        });

        createRoomButton.setFocusPainted(false);
        joinRoomButton.setFocusPainted(false);

        add(createRoomButton);
        add(joinRoomButton);

        // 캐릭터 이미지 로딩
        ImageIcon octo1_icon = new ImageIcon("src/main/java/image/대기1.gif");
        ImageIcon octo2_icon = new ImageIcon("src/main/java/image/대기2.gif");

        octo1 = new JLabel(octo1_icon);
        octo2 = new JLabel(octo2_icon);
        octo1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 상단에 10픽셀 여백 추가
        octo2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        octo1.setBounds(Octo1_imageX, 550, octo1_icon.getIconWidth(), octo1_icon.getIconHeight());
        octo2.setBounds(Octo2_imageX, 550, octo2_icon.getIconWidth(), octo2_icon.getIconHeight());

        add(octo1);
        add(octo2);

        // 깃발 이미지
        ImageIcon flag_gif = new ImageIcon("src/main/java/image/깃발.gif");
        JLabel flag = new JLabel(flag_gif);
        flag.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        flag.setBounds(1100, 500, flag_gif.getIconWidth(), flag_gif.getIconHeight());
        add(flag);
        
        // 배경 새 이미지
        ImageIcon bird1_icon = new ImageIcon("src/main/java/image/새1.gif");
        ImageIcon bird2_icon = new ImageIcon("src/main/java/image/새2.gif");
        ImageIcon bird3_icon = new ImageIcon("src/main/java/image/새3.gif");

        JLabel bird1 = new JLabel(bird1_icon);
        JLabel bird2 = new JLabel(bird2_icon);
        JLabel bird3 = new JLabel(bird3_icon);

        bird1.setBounds(1100, 350, bird1_icon.getIconWidth(), bird1_icon.getIconHeight());
        bird2.setBounds(1000, 300, bird2_icon.getIconWidth(), bird2_icon.getIconHeight());
        bird3.setBounds(1150, 250, bird3_icon.getIconWidth(), bird3_icon.getIconHeight());
        add(bird1);
        add(bird2);
        add(bird3);

        loadImages();  // 이미지를 한 번만 로드

        Timer timer = new Timer(15, e -> moveOcto());
        timer.start();

        bgm = new SoundPlayer("src/main/java/sound/메인.wav", -20.0f);
        if(!bgm.isPlaying()){
            bgm.play(true);
        }
    }

    private void loadImages() {
        try {
            // 배경 이미지 로딩
            background_images[0] = ImageIO.read(new File("src/main/java/image/배경1.png"));
            background_images[1] = ImageIO.read(new File("src/main/java/image/배경2.png"));
            background_images[2] = ImageIO.read(new File("src/main/java/image/배경_중간1.png"));
            background_images[3] = ImageIO.read(new File("src/main/java/image/배경_중간2.png"));
            background_images[4] = ImageIO.read(new File("src/main/java/image/배경_중간3.png"));
            background_images[5] = ImageIO.read(new File("src/main/java/image/배경_중간4.png"));
            background_images[6] = ImageIO.read(new File("src/main/java/image/깃대.png"));

            for (int i = 0; i < 22; i++) {
                int idx = i%4 + 2;
                background_middle[i] = background_images[idx];
            }

            // 바닥 이미지 로딩
            fooler_block1 = ImageIO.read(new File("src/main/java/image/풀_바닥1.png"));
            fooler_block2 = ImageIO.read(new File("src/main/java/image/풀_바닥2.png"));
            fooler_block3 = ImageIO.read(new File("src/main/java/image/풀_바닥3.png"));

            logo = ImageIO.read(new File("src/main/java/image/logo.png"));

        } catch (IOException e) {
            System.out.println("이미지 로드 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void moveOcto() {
        Octo1_imageX += Octo1_speed;
        Octo2_imageX += Octo2_speed;
        if (Octo1_imageX > getWidth())
            Octo1_imageX = -octo1.getWidth();
        if (Octo2_imageX > getWidth())
            Octo2_imageX = -octo2.getWidth();

        octo1.setLocation(Octo1_imageX, 550);
        octo2.setLocation(Octo2_imageX, 550);
        revalidate();
        repaint();  // 화면을 다시 그립니다.
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

        for (int x = 0, i=0; x < width; x += 60) {
            g.drawImage(background_middle[i], x, 420, 60, 60, this);
            i++;
        }

        // 바닥 1~3 그리기
        g.drawImage(fooler_block1, 0, 625, 60, 60, this);
        for (int x = 60; x < width - 60; x += 60) {
            g.drawImage(fooler_block2, x, 625, 60, 60, this);
        }
        g.drawImage(fooler_block3, width - 60, 625, 60, 60, this);

        g.drawImage(logo, 400, 40, 460, 240, this);

        g.drawImage(background_images[6], 1100, 580, 50, 50, this);
        g.drawImage(background_images[6], 1100, 540, 50, 50, this);
    }
}