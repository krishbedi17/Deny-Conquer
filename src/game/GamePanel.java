package game;

import network.Client;
import network.MessageToSend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GamePanel extends JPanel implements MouseListener, MouseMotionListener {
    private final GameBoard board;
    private Cell cellBeingDrawnOn = null;
    private Color playerColor;
    MessageToSend lastMsg;
    MessageToSend requestMsg;
    private boolean gameOver = false;

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
        if(gameOver){
            return;
        }
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
        if(gameOver){
            return;
        }
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
            String color = checkWinCondition();
            if(color!=null){
                Color winnerColor = getColorFromName(color);
                MessageToSend winMsg = new MessageToSend(-1, -1, new Point(0, 0), winnerColor, "GameOver", "0");
                player.sendMessage(winMsg);
                gameOver = true;
            }
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

    public String checkWinCondition(){
        HashMap<String, Integer> colorCounts = new HashMap<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Cell cell = board.getCellByRowAndCol(row, col);
                Color color = cell.getColorOfCell();
                String colorName = getColorName(color);
                if(colorName.equalsIgnoreCase("WHITE")){
                    return null;
                }
                colorCounts.put(colorName, colorCounts.getOrDefault(colorName, 0) + 1);
            }
        }
        String winner = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : colorCounts.entrySet()){
            if(entry.getValue()> maxCount){
                winner = entry.getKey();
                maxCount = entry.getValue();
            }
        }
        return winner;

    }
    private String getColorName(Color color) {
        if (color.equals(Color.RED)) return "RED";
        if (color.equals(Color.BLUE)) return "BLUE";
        if (color.equals(Color.GREEN)) return "GREEN";
        if (color.equals(Color.ORANGE)) return "ORANGE";
        if (color.equals(Color.MAGENTA)) return "MAGENTA";
        if (color.equals(Color.WHITE)) return "WHITE";
        return "UNKNOWN";
    }
    private Color getColorFromName(String name) {
        switch (name.toUpperCase()) {
            case "RED": return Color.RED;
            case "BLUE": return Color.BLUE;
            case "GREEN": return Color.GREEN;
            case "ORANGE": return Color.ORANGE;
            case "MAGENTA": return Color.MAGENTA;
            default: return Color.GRAY;
        }
    }
}
