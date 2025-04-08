package game;

import javax.swing.*;
import java.io.IOException;

public class GameFrame extends JFrame {
    public GameFrame() throws IOException {
        setTitle("Deny and Conquer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
        pack();

        setLocationRelativeTo(null); // Center window
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new GameFrame();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Could not connect to server:\n" + e.getMessage(),
                        "Connection Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });

    }
}
