package game;

import network.Client;
import network.MessageToSend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class GamePanel extends JPanel implements MouseListener, MouseMotionListener {
    private final GameBoard board;
    private Cell cellBeingDrawnOn = null;
    private Color playerColor;
    MessageToSend lastMsg;
    MessageToSend requestMsg;

    Client player;

    public GamePanel(Color selectedColor) throws IOException {
        this.board = new GameBoard();
        player = new Client(this);
        this.playerColor = selectedColor;

        setPreferredSize(new Dimension(50 * 8, 50 * 8));
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        board.drawBoard((Graphics2D) g);
    }

    @Override
    public void mousePressed(MouseEvent e) {

        boolean gotLock = player.requestLockAndWait(e.getX()/50, e.getY()/50, new Point(e.getX(), e.getY()));
        if (!gotLock) {
            System.out.println("Cell lock denied — input blocked.");
            return;
        }
        Cell cell = board.getCellAtPixel(e.getX(), e.getY());
        requestMsg = new MessageToSend(e.getX()/50, e.getY()/50, new Point(e.getX(), e.getY()), Color.BLACK, "Request","-1");
        player.sendMessage(requestMsg);
        if (cell != null && !cell.isClaimed() && !cell.isBeingClaimed()) {
            cell.setBeingClaimed(true); // probably some mutexing
            cellBeingDrawnOn = cell;
            // add cell to server list
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        drawPixel(cellBeingDrawnOn, e.getX(), e.getY(), playerColor);
    }

    public void drawPixel(Cell cell,  int pixelX, int pixelY, Color color) {
        if (cell != null) {
            if (checkIfStillInsideCell(cell, pixelX, pixelY)) {
                int x = pixelX % 50;
                int y = pixelY % 50;
                cell.addDrawnPixel(x, y, color);
                repaint();

                lastMsg = new MessageToSend(pixelX/50, pixelY/50, new Point(x, y), color, "Scribble","-1");
                player.sendMessage(lastMsg);
            }
        } else {
            // do nothing
        }
    }

    private boolean checkIfStillInsideCell(Cell cell,  int x, int y) {
        Point startPoint = cell.locOnCanvas;
        boolean flagX = (startPoint.x <= x && startPoint.x + 50 > x);
        boolean flagY = (startPoint.y <= y && startPoint.y + 50 > y);
        return (flagX && flagY);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (cellBeingDrawnOn != null) {
            cellBeingDrawnOn.checkIfValidFill(playerColor);
            cellBeingDrawnOn = null;
            repaint();

            MessageToSend mouseReleaseMsg = new MessageToSend(lastMsg.getRow(), lastMsg.getCol(), lastMsg.getPixel(), lastMsg.getPlayerColor(), "Release","-1");
            player.sendMessage(mouseReleaseMsg);
        }
    }

    // Unused events
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}

    public Cell getCell(int col, int row) {
        return board.getCellByRowAndCol(row, col);
    }
}
