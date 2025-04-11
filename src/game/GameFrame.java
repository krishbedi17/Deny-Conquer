package game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import com.formdev.flatlaf.FlatLightLaf;

public class GameFrame extends JFrame {
    public GameFrame() throws IOException {
        setTitle("Deny and Conquer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        FlatLightLaf.setup();

        UIManager.put("Label.font", new Font("SansSerif", Font.PLAIN, 14));
        UIManager.put("Button.font", new Font("SansSerif", Font.PLAIN, 14));

        WelcomePanel.UserSelection userSelection = WelcomePanel.showDialog(this);
        String username = userSelection.getUsername();
        Color selectedColor = userSelection.getColor();



        GamePanel gamePanel = new GamePanel(selectedColor);
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
