package game;

import javax.swing.*;
import java.awt.*;

public class WelcomePanel {

    /**
     * Displays a series of dialogs to:
     * 1. Ask for a username.
     * 2. Show the game rules.
     * 3. Allow the user to pick one of five predefined colors.
     *
     * @param parent the parent component for the dialogs.
     * @return A UserSelection object containing the username and chosen color.
     */
    public static UserSelection showDialog(Component parent) {
        // 1. Ask for username
        String username = JOptionPane.showInputDialog(
                parent,
                "Enter your username:",
                "Username",
                JOptionPane.PLAIN_MESSAGE
        );
        if (username == null || username.trim().isEmpty()) {
            username = "Guest";
        }

        // 2. Show the game rules
        String rules = "Game Rules:\n" +
                "1. Claim a cell by drawing within it.\n" +
                "2. Only one cell can be claimed at a time.\n" +
                "3. Use your selected color to mark your territory.\n" +
                "4. The game ends when all cells are claimed.\n" +
                "5. The player with the most claimed cells wins!";
        JOptionPane.showMessageDialog(
                parent,
                rules,
                "Game Rules",
                JOptionPane.INFORMATION_MESSAGE
        );

        // 3. Provide a list of 5 colors to choose from
        String[] colorNames = {"Green", "Red", "Blue", "Orange", "Magenta"};
        Color[] colors = {Color.GREEN, Color.RED, Color.BLUE, Color.ORANGE, Color.MAGENTA};
        String chosenColorName = (String) JOptionPane.showInputDialog(
                parent,
                "Choose Your Player Color:",
                "Color Selection",
                JOptionPane.PLAIN_MESSAGE,
                null,
                colorNames,
                colorNames[0]
        );

        Color chosenColor = Color.GRAY; // default fallback
        for (int i = 0; i < colorNames.length; i++) {
            if (colorNames[i].equals(chosenColorName)) {
                chosenColor = colors[i];
                break;
            }
        }

        return new UserSelection(username, chosenColor);
    }

    /**
     * A simple class to hold the user's selection data.
     */
    public static class UserSelection {
        private final String username;
        private final Color color;

        public UserSelection(String username, Color color) {
            this.username = username;
            this.color = color;
        }

        public String getUsername() {
            return username;
        }

        public Color getColor() {
            return color;
        }
    }

    public static String getColorName(Color color) {
        if (color.equals(Color.RED)) return "RED";
        if (color.equals(Color.BLUE)) return "BLUE";
        if (color.equals(Color.GREEN)) return "GREEN";
        if (color.equals(Color.ORANGE)) return "ORANGE";
        if (color.equals(Color.MAGENTA)) return "MAGENTA";
        if (color.equals(Color.WHITE)) return "WHITE";
        return "UNKNOWN";
    }
    public static Color getColorFromName(String name) {
        switch (name.toUpperCase()) {
            case "RED": return Color.RED;
            case "BLUE": return Color.BLUE;
            case "GREEN": return Color.GREEN;
            case "ORANGE": return Color.ORANGE;
            case "MAGENTA": return Color.MAGENTA;
            case "WHITE": return Color.WHITE;
            default: return Color.GRAY;
        }
    }
}