import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Panel1 extends JPanel {
	public Panel1(MainFrame frame) {
        setLayout(null);
        JLabel label = new JLabel("����� Panel1�Դϴ�.");
        label.setBounds(580, 300, 120, 40);
        
        JButton backButton = new JButton("������");
        backButton.setBounds(1150, 0, 120, 40);
        backButton.addActionListener(e -> frame.showPanel("MainPanel"));

        add(label);
        add(backButton);
    }
}