package game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import com.formdev.flatlaf.FlatLightLaf;

public class GameFrame extends JFrame {
    private JLabel statusLabel;

    public GameFrame() throws IOException {
        setTitle("Deny and Conquer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        FlatLightLaf.setup();

        UIManager.put("Label.font", new Font("SansSerif", Font.PLAIN, 14));
        UIManager.put("Button.font", new Font("SansSerif", Font.PLAIN, 14));

        // Show the dialog for username and color selection
        WelcomePanel.UserSelection userSelection = WelcomePanel.showDialog(this);
        String username = userSelection.getUsername();
        Color selectedColor = userSelection.getColor();

        // Create the game panel
        gamePanel gamePanel = new gamePanel(selectedColor, username);

        // Create the status panel
        statusLabel = new JLabel("Welcome, " + username + "! Choose a cell to begin.");
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Add game panel and status panel to the frame
        add(gamePanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        // Set the status label in the game panel for interaction
        gamePanel.setStatusLabel(statusLabel);

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