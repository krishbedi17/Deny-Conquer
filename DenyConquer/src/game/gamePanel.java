package game;

import Client.MessageToSend;
import Client.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class gamePanel extends JPanel implements MouseListener, MouseMotionListener {
    private final GameBoard board;
    private Cell cellBeingDrawnOn = null;
    private Color playerColor;
    private final String username;
    private JLabel statusLabel; // Reference to the status label
    MessageToSend Msg;
    private int currentCellRow = -1;
    private int currentCellCol = -1;

    client player;
    public gamePanel(Color selectedColor, String username) throws IOException {
        this.board = new GameBoard();
        playerColor = selectedColor;
        this.player = new client(this);
        this.username = username;
        setPreferredSize(new Dimension(50 * 8, 50 * 8));
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(JLabel statusLabel) {
        this.statusLabel = statusLabel;
    }

    public void setCellBeingDrawnOn(Cell cellBeingDrawnOn) {
        this.cellBeingDrawnOn = cellBeingDrawnOn;
    }

    public String getUsername() {
        return username;
    }

    public int getCurrentCellRow() {
        return currentCellRow;
    }

    public int getCurrentCellCol() {
        return currentCellCol;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        board.drawBoard((Graphics2D) g);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //check lock
        currentCellRow = e.getY() / 50;
        currentCellCol = e.getX() / 50;
        Msg = new MessageToSend(currentCellRow,currentCellCol,new Point(e.getX(),e.getY()),Color.GRAY,"RequestLock",player.getClientID());
        player.sendMessage(Msg);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        //check lock
        //if granted lock then proceed
        //else give error
        drawPixel(cellBeingDrawnOn, e.getX(), e.getY(), playerColor);
    }

    public void drawPixel(Cell cell, int pixelX, int pixelY, Color color) {
        if (cell != null) {
            if (checkIfStillInsideCell(cell, pixelX, pixelY)) {
                int x = pixelX % 50;
                int y = pixelY % 50;
                cell.addDrawnPixel(x, y, color);
                repaint();

                // Make sure to use the stored row and column
                Msg = new MessageToSend(currentCellRow, currentCellCol, new Point(pixelX, pixelY), color, "Scribble",player.getClientID());
                player.sendMessage(Msg);

                // Update the status label
                if (statusLabel != null) {
                    statusLabel.setText(username + " is drawing on the cell at (" + (currentCellRow + 1) + ", " + (currentCellCol + 1) + ").");
                }
            }
        }
    }

    private boolean checkIfStillInsideCell(Cell cell, int x, int y) {
        Point startPoint = cell.startPoint;
        boolean flagX = (startPoint.x <= x && startPoint.x + 50 > x);
        boolean flagY = (startPoint.y <= y && startPoint.y + 50 > y);
        return (flagX && flagY);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (cellBeingDrawnOn != null) {
            boolean filled = cellBeingDrawnOn.checkIfValidFill(playerColor);

            String messageType = filled ? "Filled" : "Unlock";
            MessageToSend releaseMsg = new MessageToSend(currentCellRow, currentCellCol,
                    new Point(e.getX() % 50, e.getY() % 50),
                    playerColor, messageType,player.getClientID());
            player.sendMessage(releaseMsg);

            // Update the status label
            if (statusLabel != null) {
                if (filled) {
                    statusLabel.setText(username + " successfully claimed the cell at (" + (currentCellRow + 1) + ", " + (currentCellCol + 1) + ")!");
                } else {
                    statusLabel.setText(username + " released the cell at (" + (currentCellRow + 1) + ", " + (currentCellCol + 1) + ").");
                }
            }

            cellBeingDrawnOn = null;
            currentCellRow = -1;
            currentCellCol = -1;
            repaint();
        }
    }

    // Unused events
    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    public Cell getCell(int row, int col) {
        return board.getCellByRowAndCol(row, col);
    }
}
