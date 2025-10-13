package main;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.Color;

import javax.swing.JFrame;

public class GameWindow {
    private JFrame frame;
    public GameWindow(Gamepanel gamePanel){
        frame = new JFrame();
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(gamePanel);
        frame.setResizable(false);

        // pack before centering so size is correct
        frame.pack();
        frame.setLocationRelativeTo(null);

        // make the game panel background grey
        // gamePanel.setBackground(Color.GRAY);

        frame.setVisible(true);
        frame.addWindowFocusListener(new WindowFocusListener() {

            @Override
            public void windowGainedFocus(WindowEvent e) {
               gamePanel.getGame().WindowrequestFocus();
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
              
            }
            
        });

    }
}
