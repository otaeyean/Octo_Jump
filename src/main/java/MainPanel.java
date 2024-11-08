import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class MainPanel extends JPanel {

    private BufferedImage[] background_images = new BufferedImage[6];
    private BufferedImage[] background_middle = new BufferedImage[30];
    private BufferedImage fooler_block1, fooler_block2, fooler_block3;
    private JLabel octo1, octo2;

    private int Octo1_imageX = 0;
    private int Octo2_imageX = 0;
    private int Octo1_speed = 3;
    private int Octo2_speed = 2;

    public MainPanel(MainFrame frame) {
        setLayout(null);

        JButton createRoomButton = new JButton("방 만들기");
        JButton joinRoomButton = new JButton("참여하기");

        createRoomButton.setBounds(580, 300, 120, 40); // 중앙에 위치
        joinRoomButton.setBounds(580, 360, 120, 40);

        createRoomButton.addActionListener(e -> frame.showPanel("Panel1"));
        joinRoomButton.addActionListener(e -> frame.showPanel("Panel2"));

        createRoomButton.setFocusPainted(false);
        joinRoomButton.setFocusPainted(false);

        add(createRoomButton);
        add(joinRoomButton);

        // 캐릭터 이미지 로딩
        ImageIcon octo1_icon = new ImageIcon("src/main/java/image/대기2.gif");
        ImageIcon octo2_icon = new ImageIcon("src/main/java/image/대기2.gif");
        Image octo1_scaled = octo1_icon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
        Image octo2_scaled = octo2_icon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);

        octo1 = new JLabel(new ImageIcon(octo1_scaled));
        octo2 = new JLabel(new ImageIcon(octo2_scaled));
        octo1.setBounds(Octo1_imageX, 570, octo1_icon.getIconWidth(), octo1_icon.getIconHeight());
        octo2.setBounds(Octo2_imageX, 570, octo2_icon.getIconWidth(), octo2_icon.getIconHeight());

        add(octo1);
        add(octo2);

        loadImages();  // 이미지를 한 번만 로드

        revalidate();
        repaint();

        Timer timer = new Timer(5, e -> moveImage());
        timer.start();
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

            for (int i = 0; i < 22; i++) {
                int idx = i%4 + 2;
                background_middle[i] = background_images[idx];
            }

            // 바닥 이미지 로딩
            fooler_block1 = ImageIO.read(new File("src/main/java/image/풀_바닥1.png"));
            fooler_block2 = ImageIO.read(new File("src/main/java/image/풀_바닥2.png"));
            fooler_block3 = ImageIO.read(new File("src/main/java/image/풀_바닥3.png"));

        } catch (IOException e) {
            System.out.println("이미지 로드 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void moveImage() {
        Octo1_imageX += Octo1_speed;
        Octo2_imageX += Octo2_speed;
        if (Octo1_imageX > getWidth())
            Octo1_imageX = -octo1.getWidth();
        if (Octo2_imageX > getWidth())
            Octo2_imageX = -octo2.getWidth();

        octo1.setLocation(Octo1_imageX, 570);
        octo2.setLocation(Octo2_imageX, 570);
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
    }
}