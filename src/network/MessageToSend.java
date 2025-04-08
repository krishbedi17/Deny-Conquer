package network;

import java.awt.*;
import java.io.Serializable;

public class MessageToSend implements Serializable {
    int row, col;
    Point pixel;
    Color playerColor;
    String type;

    public MessageToSend(int row, int col, Point pixel, Color playerColor, String type) {
        this.row = row;
        this.col = col;
        this.pixel = pixel;
        this.playerColor = playerColor;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public Point getPixel() {
        return pixel;
    }

    public void setPixel(Point pixel) {
        this.pixel = pixel;
    }

    public Color getPlayerColor() {
        return playerColor;
    }

    public void setPlayerColor(Color playerColor) {
        this.playerColor = playerColor;
    }
}
