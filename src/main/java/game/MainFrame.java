package main.java.game;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JPanel mainPanel;
    private CardLayout cardLayout;

    public MainFrame() {
        setTitle("Octo Jump");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // CardLayout 설정
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 패널 생성 및 추가
        MainPanel mainMenuPanel = new MainPanel(this);
        Panel1 panel1 = new Panel1(this);
        Panel2 panel2 = new Panel2(this);
        Panel3 panel3 = new Panel3(this);

        // 각 패널을 CardLayout에 추가
        mainPanel.add(mainMenuPanel, "main.java.game.MainPanel");
        mainPanel.add(panel1, "main.java.game.Panel1");
        mainPanel.add(panel2, "main.java.game.Panel2");
        mainPanel.add(panel3, "main.java.game.Panel3");

        add(mainPanel); // 메인 패널을 프레임에 추가

        setVisible(true);
    }

    // 패널 전환 메소드
    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName); // CardLayout을 사용하여 패널 전환
    }
}
