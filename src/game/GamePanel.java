package game;

import network.Client;
import network.MessageToSend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class GamePanel extends JPanel {

    private final GameBoard board;
    private Cell cellBeingDrawnOn = null;
    private final Color playerColor;
    private MessageToSend lastMsg;
    private boolean gameOver = false;
    private final Client player;
    private final String username;

    // UI elements
    private final JLabel statusLabel;
    private final BoardPanel boardPanel;

    /**
     * Constructor accepting the player's username and chosen color.
     *
     * @param username      the player's username
     * @param selectedColor the player's chosen color
     * @throws IOException if there's an issue initializing the client connection
     */
    public GamePanel(String username, Color selectedColor) throws IOException {
        this.username = username;
        this.playerColor = selectedColor;
        this.board = new GameBoard();
        this.player = new Client(this);

        // Set overall layout and background.
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        // Create a status label at the bottom.
        statusLabel = new JLabel("Logged in as: " + username, SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(statusLabel, BorderLayout.SOUTH);

        // Create the board panel for drawing.
        boardPanel = new BoardPanel();
        boardPanel.setPreferredSize(new Dimension(400, 400));
        boardPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Center the boardPanel using a container with GridBagLayout.
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setOpaque(false); // transparent container so the background shows.
        centerContainer.add(boardPanel);

        add(centerContainer, BorderLayout.CENTER);
    }

    /**
     * The inner class that handles custom drawing of the game board.
     * Mouse listeners are attached to this panel.
     */
    private class BoardPanel extends JPanel implements MouseListener, MouseMotionListener {

        public BoardPanel() {
            setBackground(Color.WHITE);
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Enable anti-aliasing.
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            board.drawBoard(g2);
        }

        // MouseListener & MouseMotionListener implementations.
        @Override
        public void mousePressed(MouseEvent e) {
            if (gameOver) {
                return;
            }
            int gridSize = 50;
            int col = e.getX() / gridSize;
            int row = e.getY() / gridSize;
            // Debug output (optional)
            System.out.println("Pressed at: " + e.getX() + ", " + e.getY() + " computed cell(" + row + "," + col + ")");

            boolean gotLock = player.requestLockAndWait(row, col, new Point(e.getX(), e.getY()));
            if (!gotLock) {
                statusLabel.setText("Cell lock denied — try another cell.");
                return;
            } else {
                statusLabel.setText("Cell lock granted. Start filling...");
            }

            Cell cell = board.getCellAtPixel(e.getX(), e.getY());
            if (cell != null && !cell.isClaimed() && !cell.isBeingClaimed()) {
                cell.setBeingClaimed(true);
                cellBeingDrawnOn = cell;
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!player.getLockGranted()) {
                return;
            }
            if (gameOver) return;
            // Update status while drawing.
            statusLabel.setText("Filling cell...");
            drawPixel(cellBeingDrawnOn, e.getX(), e.getY(), playerColor);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (!player.getLockGranted()) {
                return;
            }
            if (cellBeingDrawnOn != null) {
                System.out.println("Inside mouse released");
                boolean filled = cellBeingDrawnOn.checkIfValidFill(playerColor);
                cellBeingDrawnOn = null;
                repaint();
                String messageType = "Release";
                if (filled) {
                    messageType += "Filled";
                } else {
                    messageType += "NotFilled";
                }
                if (lastMsg != null) {
                    MessageToSend releaseMsg = new MessageToSend(lastMsg.getRow(), lastMsg.getCol(), lastMsg.getPixel(), lastMsg.getPlayerColor(), messageType, player.getClientID());
                    player.sendMessage(releaseMsg);
                    player.setLockGranted(false); // maybe should do this after
                }

                if (filled) {
                    statusLabel.setText("Cell filled successfully!");
                } else {
                    statusLabel.setText("Incomplete filling. Try again.");
                }

                String winColor = checkWinCondition();
                if (winColor != null) {
                    Color winnerColor = getColorFromName(winColor);
                    MessageToSend winMsg = new MessageToSend(-1, -1, new Point(0, 0), winnerColor, "GameOver", player.getClientID());
                    player.sendMessage(winMsg);
                    gameOver = true;
                    statusLabel.setText("Game Over! Winner: " + winColor);
                }
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }
    }

    /**
     * Helper method to draw a pixel on the current cell.
     */
    public void drawPixel(Cell cell, int pixelX, int pixelY, Color color) {
        if (cell != null && checkIfStillInsideCell(cell, pixelX, pixelY)) {
            int x = pixelX % 50;
            int y = pixelY % 50;
            cell.addDrawnPixel(x, y, color);
            boardPanel.repaint();
            lastMsg = new MessageToSend(pixelX / 50, pixelY / 50, new Point(x, y), color, "Scribble", player.getClientID());
            player.sendMessage(lastMsg);
        }
    }

    private boolean checkIfStillInsideCell(Cell cell, int x, int y) {
        Point startPoint = cell.locOnCanvas;
        return (startPoint.x <= x && x < startPoint.x + 50) &&
                (startPoint.y <= y && y < startPoint.y + 50);
    }

    public Cell getCell(int col, int row) {
        return board.getCellByRowAndCol(row, col);
    }

    public String checkWinCondition() {
        java.util.HashMap<String, Integer> colorCounts = new java.util.HashMap<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Cell cell = board.getCellByRowAndCol(row, col);
                Color color = cell.getColorOfCell();
                String colorName = getColorName(color);
                if (colorName.equalsIgnoreCase("WHITE")) {
                    return null;
                }
                colorCounts.put(colorName, colorCounts.getOrDefault(colorName, 0) + 1);
            }
        }
        String winner = null;
        int maxCount = 0;
        for (java.util.Map.Entry<String, Integer> entry : colorCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
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
