package game;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Objects;
import java.util.Random;

public class Panel2 extends JPanel {
    private BufferedImage[] background_images = new BufferedImage[20];
    private BufferedImage[] background_middle = new BufferedImage[30];
    private BufferedImage fooler_block1, fooler_block2, fooler_block3, logo;
    private JLabel octo1, octo2;
    private boolean isJumping1 = false, isJumping2 = false; // 독립적으로 관리되는 점프 상태
    private final Random random = new Random();
    private Client client = new Client();

    private static SoundPlayer bgm, jump;

    public Panel2(MainFrame frame) {
        setLayout(null);

        // 캐릭터 이미지 로딩
        ImageIcon octo1_icon = new ImageIcon("src/main/java/image/대기1.gif");
        ImageIcon octo2_icon = new ImageIcon("src/main/java/image/대기2.gif");

        octo1 = new JLabel(octo1_icon);
        octo2 = new JLabel(octo2_icon);

        octo1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        octo2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 옥토퍼스 1,2 초기 위치 설정
        octo1.setBounds(300, 550, octo1_icon.getIconWidth(), octo1_icon.getIconHeight());
        octo2.setBounds(500, 550, octo2_icon.getIconWidth(), octo2_icon.getIconHeight());

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

        loadImages(); // 이미지를 한 번만 로드

        setupJumpTimers(); // 점프 타이머 설정

        setupConnectionUI(frame); // 서버 연결 UI 설정

        bgm = new SoundPlayer("src/main/java/sound/게임시작2.wav", -25.0f);
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
            background_images[7] = ImageIO.read(new File("src/main/java/image/나무1.png"));
            background_images[8] = ImageIO.read(new File("src/main/java/image/나무2.png"));
            background_images[9] = ImageIO.read(new File("src/main/java/image/나무3.png"));
            background_images[10] = ImageIO.read(new File("src/main/java/image/나무4.png"));
            background_images[11] = ImageIO.read(new File("src/main/java/image/선인장.png"));

            for (int i = 0; i < 22; i++) {
                int idx = i % 4 + 2;
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

    private void setupConnectionUI(MainFrame frame) {
        // 주소 입력 필드
        JLabel addressLabel = new JLabel("서버 주소:");
        addressLabel.setBounds(500, 250, 100, 30);
        add(addressLabel);

        JTextField addressField = new JTextField(15);
        addressField.setBounds(600, 250, 150, 30);
        add(addressField);


        // 참가 버튼
        JButton joinButton = new JButton("참가");
        joinButton.setFocusPainted(false);
        joinButton.setBounds(580, 320, 100, 40);
        joinButton.addActionListener(e -> {
            String ip = addressField.getText();
            System.out.println("서버 주소: " + ip);

            client.connect(ip, 5000);
            System.out.println(client.isConnected());
            if(client.isConnected()){
                frame.showGamePanel(client);
                bgm.stop();
            }
            else{
                JOptionPane.showMessageDialog(
                        this, // 현재 패널을 부모 컴포넌트로 사용
                        "서버와 연결에 실패했습니다. 다시 시도해주세요.",
                        "연결 실패",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
        add(joinButton);

        JButton backButton = new JButton("나가기");
        backButton.setBounds(1150, 0, 120, 40);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.showPanel("main.java.game.MainPanel");
                bgm.stop();
            }
        });
        backButton.setFocusPainted(false);
        add(backButton);
    }

    private void setupJumpTimers() {
        // 옥토퍼스 1의 점프 타이머
        Timer jumpTimer1 = new Timer(1000 + random.nextInt(1000), e -> {
            if (!isJumping1) startJump(octo1, () -> isJumping1 = false);
        });

        // 옥토퍼스 2의 점프 타이머
        Timer jumpTimer2 = new Timer(1000 + random.nextInt(1000), e -> {
            if (!isJumping2) startJump(octo2, () -> isJumping2 = false);
        });

        jumpTimer1.start();
        jumpTimer2.start();
    }

    private void startJump(JLabel character, Runnable onJumpComplete) {
        final int[] characterY = {character.getY()};
        final int INITIAL_JUMP_VELOCITY = 18;

        // 점프 상태 플래그와 중복 점프 방지
        if (character == octo1 && isJumping1) return;
        if (character == octo2 && isJumping2) return;

        if (character == octo1) isJumping1 = true;
        if (character == octo2) isJumping2 = true;

        Timer jumpTimer = new Timer(16, new ActionListener() {
            private int velocity = -INITIAL_JUMP_VELOCITY; // 초기 점프 속도
            private final int gravity = 1; // 중력
            private final int initialY = characterY[0]; // 바닥 위치

            @Override
            public void actionPerformed(ActionEvent e) {
                // 새로운 위치 계산
                characterY[0] += velocity;

                // 중력 효과 적용
                velocity += gravity;

                // 캐릭터 위치 업데이트
                character.setLocation(character.getX(), characterY[0]);

                // 바닥에 도달하거나 캐릭터가 내려와야 할 때
                if (characterY[0] >= initialY) {
                    characterY[0] = initialY; // 바닥 위치로 조정
                    ((Timer) e.getSource()).stop(); // 타이머 중지

                    // 점프 상태 플래그 해제
                    if (character == octo1) isJumping1 = false;
                    if (character == octo2) isJumping2 = false;

                    onJumpComplete.run(); // 점프 종료 콜백
                }
            }
        });

        jumpTimer.start();
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

        g.drawImage(background_images[6], 1100, 580, 50, 50, this);
        g.drawImage(background_images[6], 1100, 540, 50, 50, this);

        // 나무
        g.drawImage(background_images[10], 100, 500, 50, 50, this);
        g.drawImage(background_images[9], 100, 540, 50, 50, this);
        g.drawImage(background_images[8], 100, 580, 50, 50, this);
        g.drawImage(background_images[7], 55, 420, 50, 50, this);
        g.drawImage(background_images[7], 55, 500, 50, 50, this);
        g.drawImage(background_images[7], 55, 460, 50, 50, this);
        g.drawImage(background_images[7], 100, 460, 50, 50, this);
        g.drawImage(background_images[7], 100, 500, 50, 50, this);
        g.drawImage(background_images[7], 100, 420, 50, 50, this);
        g.drawImage(background_images[7], 145, 460, 50, 50, this);
        g.drawImage(background_images[7], 145, 500, 50, 50, this);
        g.drawImage(background_images[7], 145, 420, 50, 50, this);
        g.drawImage(background_images[11], 1005, 580, 50, 50, this);
        g.drawImage(background_images[11], 955, 580, 50, 50, this);
    }
}
