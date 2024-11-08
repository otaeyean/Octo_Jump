import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Panel2 extends JPanel {
	public Panel2(MainFrame frame) {
        setLayout(null);
        JLabel label = new JLabel("여기는 Panel2입니다.");
        label.setBounds(580, 300, 120, 40);
        
        JButton joinButton = new JButton("참여");
        joinButton.setBounds(580, 360, 120, 40);
        joinButton.addActionListener(e -> frame.showPanel("Panel1"));

        add(label);
        add(joinButton);
    }
}