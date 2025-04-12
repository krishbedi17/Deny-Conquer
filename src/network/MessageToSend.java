package network;

import java.awt.*;
import java.io.Serializable;

public class MessageToSend implements Serializable {
    private static final long serialVersionUID = 1L;

    int row, col;
    SerializablePoint pixel; // Changed from Point to SerializablePoint
    Color playerColor;
    String type;
    String senderID;

    public MessageToSend(int row, int col, Point pixel, Color playerColor, String type) {
        this.row = row;
        this.col = col;
        this.pixel = new SerializablePoint(pixel.x, pixel.y); // Convert Point to SerializablePoint
        this.playerColor = playerColor;
        this.type = type;
        this.senderID = senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getSenderID() {
        return senderID;
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
        return new Point(pixel.x, pixel.y); // Convert SerializablePoint back to Point
    }

    public void setPixel(Point pixel) {
        this.pixel = new SerializablePoint(pixel.x, pixel.y);
    }

    public Color getPlayerColor() {
        return playerColor;
    }

    public void setPlayerColor(Color playerColor) {
        this.playerColor = playerColor;
    }

    // Inner class to make Point serializable
    public static class SerializablePoint implements Serializable {
        private static final long serialVersionUID = 1L;

        int x, y;

        public SerializablePoint(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}