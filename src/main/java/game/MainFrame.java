package game;

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

        // 처음 화면에 보여줄 패널 생성 및 추가
        MainPanel mainMenuPanel = new MainPanel(this);
        mainPanel.add(mainMenuPanel, "main.java.game.MainPanel");

        add(mainPanel); // 메인 패널을 프레임에 추가

        setVisible(true);
    }

    // 패널 전환 메소드
    public void showPanel(String panelName) {
        // 기존 패널 모두 제거
        mainPanel.removeAll();

        // 새로운 패널 생성 및 추가
        JPanel newPanel = createPanel(panelName);
        mainPanel.add(newPanel, panelName);

        // 패널 전환
        cardLayout.show(mainPanel, panelName);

        // UI를 새로 고침
        revalidate();
        repaint();
    }

    // 패널 전환 메소드
    public void showGamePanel(Client client) {
        mainPanel.removeAll();

        JPanel newPanel = createGamePanel(client);
        mainPanel.add(newPanel, "main.java.game.Panel3");

        cardLayout.show(mainPanel, "main.java.game.Panel3");

        SwingUtilities.invokeLater(() -> {
            newPanel.requestFocusInWindow();
        });

        revalidate();
        repaint();
    }

    // 패널 이름에 맞는 패널을 생성하는 메소드
    private JPanel createPanel(String panelName) {
        switch (panelName) {
            case "main.java.game.MainPanel":
                return new MainPanel(this);
            case "main.java.game.Panel1":
                return new Panel1(this);
            case "main.java.game.Panel2":
                return new Panel2(this);
            case "main.java.game.Panel4.win1":
                return new Panel4(this, "octo1");
            case "main.java.game.Panel4.win2":
                return new Panel4(this, "octo2");
            default:
                throw new IllegalArgumentException("Unknown panel: " + panelName);
        }
    }

    private JPanel createGamePanel(Client client){
        return new Panel3(this, client);
    }
}
