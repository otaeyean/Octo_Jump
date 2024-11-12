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

        // CardLayout ����
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // �г� ���� �� �߰�
        MainPanel mainMenuPanel = new MainPanel(this);
        Panel1 panel1 = new Panel1(this);
        Panel2 panel2 = new Panel2(this);

        // �� �г��� CardLayout�� �߰�
        mainPanel.add(mainMenuPanel, "MainPanel");
        mainPanel.add(panel1, "Panel1");
        mainPanel.add(panel2, "Panel2");

        add(mainPanel); // ���� �г��� �����ӿ� �߰�

        setVisible(true);
    }

    // �г� ��ȯ �޼ҵ�
    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName); // CardLayout�� ����Ͽ� �г� ��ȯ
    }
}