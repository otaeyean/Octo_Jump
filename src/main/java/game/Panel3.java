package main.java.game;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Panel3 extends JPanel {
        public Panel3(MainFrame frame) {
                setLayout(null);
                JLabel label = new JLabel("여기는 Panel3입니다.");
                label.setBounds(580, 300, 120, 40);

                JButton backButton = new JButton("나가기");
                backButton.setBounds(1150, 0, 120, 40);
                backButton.addActionListener(e -> frame.showPanel("main.java.game.MainPanel"));

                add(label);
                add(backButton);
        }
}