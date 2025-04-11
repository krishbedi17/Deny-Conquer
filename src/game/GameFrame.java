package game;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class GameFrame extends JFrame {

    public GameFrame() throws IOException {
        // Set up the window
        setTitle("Deny and Conquer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Using FlatLaf for a modern look
        FlatLightLaf.setup();

        // Set global fonts if desired (optional)
        UIManager.put("Label.font", new Font("SansSerif", Font.PLAIN, 14));
        UIManager.put("Button.font", new Font("SansSerif", Font.PLAIN, 14));

        // Get user selection from the custom color/username dialog
        ColorPickerDialog.UserSelection userSelection = ColorPickerDialog.showDialog(this);
        String username = userSelection.getUsername();
        Color selectedColor = userSelection.getColor();

        // Create and add the main game panel, passing the chosen color and username
        GamePanel gamePanel = new GamePanel(username, selectedColor);
        add(gamePanel, BorderLayout.CENTER);

        // No menu bar is added (restart menu removed)

        pack();
        setLocationRelativeTo(null); // center on screen
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new GameFrame();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Could not connect to server:\n" + e.getMessage(),
                        "Connection Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
