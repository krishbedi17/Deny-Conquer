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
    private Color playerColor; // Placeholder for player 1 color
    MessageToSend lastMsg;
    private int currentCellRow = -1;
    private int currentCellCol = -1;

    Client player;

    public GamePanel(Color selectedColor) throws IOException {
        this.board = new GameBoard();
        player = new Client(this);
        playerColor = selectedColor;
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
        Cell cell = board.getCellAtPixel(e.getX(), e.getY());
        if (cell != null && !cell.isClaimed() && !cell.isBeingClaimed()) {
            cell.setBeingClaimed(true); // probably some mutexing
            cellBeingDrawnOn = cell;

            // Store the current cell row and column
            currentCellRow = e.getY() / 50;
            currentCellCol = e.getX() / 50;

            // Send a message to lock this cell for this player
            MessageToSend lockMsg = new MessageToSend(currentCellRow, currentCellCol,
                    new Point(e.getX() % 50, e.getY() % 50),
                    playerColor, "Lock");
            player.sendMessage(lockMsg);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
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
                lastMsg = new MessageToSend(currentCellRow, currentCellCol, new Point(pixelX, pixelY), color, "Scribble");
                player.sendMessage(lastMsg);
            }
        }
    }

    private boolean checkIfStillInsideCell(Cell cell, int x, int y) {
        Point startPoint = cell.locOnCanvas;
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
                    playerColor, messageType);
            player.sendMessage(releaseMsg);

            cellBeingDrawnOn = null;
            currentCellRow = -1;
            currentCellCol = -1;
            repaint();
            String color = checkWinCondition();
            if(color!=null && color.equals("Draw")){
                MessageToSend winMsg = new MessageToSend(0, 0, new Point(0, 0), Color.WHITE, "Draw");
                player.sendMessage(winMsg);
                return;
            }
            System.out.println("Color in mouseReleased:"+color);
            if(color!=null){
                Color winnerColor = WelcomePanel.getColorFromName(color);
                MessageToSend winMsg = new MessageToSend(0, 0, new Point(0, 0), winnerColor, "GameOver");
                player.sendMessage(winMsg);
            }
        }
    }

    // Unused events
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}

    public Cell getCell(int row, int col) {
        return board.getCellByRowAndCol(row, col);
    }

    public String checkWinCondition(){
        HashMap<String, Integer> colorCounts = new HashMap<>();
        for (int row = 0; row < board.getBoardSize(); row++) {
            for (int col = 0; col < board.getBoardSize(); col++) {
                Cell cell = board.getCellByRowAndCol(row, col);
                Color color = cell.getColorOfCell();
                String colorName = WelcomePanel.getColorName(color);
                if(colorName.equalsIgnoreCase("WHITE")){
                    return null;
                }
                colorCounts.put(colorName, colorCounts.getOrDefault(colorName, 0) + 1);
            }
        }
        System.out.println("Hello");
        String winner = null;
        int maxCount = 0;
        boolean tie = false;

        for (Map.Entry<String, Integer> entry : colorCounts.entrySet()){
            if(entry.getValue()> maxCount){
                winner = entry.getKey();
                maxCount = entry.getValue();
                tie = false;
            }
            else if (entry.getValue() == maxCount) {
                tie = true; // Found a tie
            }
        }
        if (tie) {
            return "Draw";
        }
        System.out.println("Winner: "+winner+" maxcount: "+ maxCount);
        return winner;

    }
}