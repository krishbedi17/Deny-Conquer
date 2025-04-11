package game;

import javax.swing.*;
import java.awt.*;

public class ColorPickerDialog {

    public static Color showColorPicker(Component parent) {
        String[] colorNames = {"Green", "Red", "Blue", "Orange", "Magenta"};
        Color[] colors = {Color.GREEN, Color.RED, Color.BLUE, Color.ORANGE, Color.MAGENTA};

        String selectedName = (String) JOptionPane.showInputDialog(
                parent,
                "Choose your player color:",
                "Color Selection",
                JOptionPane.PLAIN_MESSAGE,
                null,
                colorNames,
                colorNames[0]
        );

        if (selectedName == null) {
            // Default color if user closes dialog
            return Color.GRAY;
        }

        for (int i = 0; i < colorNames.length; i++) {
            if (colorNames[i].equals(selectedName)) {
                return colors[i];
            }
        }

        return Color.GRAY;
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
