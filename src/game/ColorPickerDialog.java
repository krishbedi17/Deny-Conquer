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
}
