package Client;

import game.Cell;
import game.gamePanel;
import game.WelcomePanel;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private String clientID;
    private gamePanel panel;
    public client(gamePanel panel) throws IOException {
        this.socket = new Socket("127.0.0.1", 53333);
        this.panel = panel;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.clientID = UUID.randomUUID().toString();
        // Optional: listen for server messages in another thread
        new Thread(this::listenToServer).start();
    }

    public String getClientID() {
        return clientID;
    }

    public void sendMessage(MessageToSend msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void listenToServer() {
        try {
            while (true) {
                Object response = in.readObject();

                if (response instanceof MessageToSend msg) {
                    System.out.println("Received message of type: " + msg.getType());

                    SwingUtilities.invokeLater(() -> {
                        Cell cell = panel.getCell(msg.row, msg.col);
                        JLabel statusLabel = panel.getStatusLabel();
                        String username = panel.getUsername();
                        int currentRow = panel.getCurrentCellRow();
                        int currentCol = panel.getCurrentCellCol();
                        if (cell != null) {
                            switch (msg.getType()) {
                                case "LockGranted":
                                    SwingUtilities.invokeLater(() -> {
                                        if (cell != null) {
                                            cell.setBeingClaimed(true);
                                            panel.setCellBeingDrawnOn(cell);
                                            panel.setCellBeingDrawnOn(cell);
                                            panel.repaint();
                                            if (statusLabel != null) {
                                                statusLabel.setText(username + " locked the cell at (" + (currentRow + 1) + ", " + (currentCol + 1) + "). Start drawing!");
                                        }}
                                });
                                    panel.repaint();
                                    break;
                                case "LockDenied":
                                    SwingUtilities.invokeLater(() -> {
                                        JOptionPane.showMessageDialog(panel, "Cell is already locked!");
                                    });
                                    if (statusLabel != null) {
                                        statusLabel.setText("Cell is already claimed or being drawn on!");
                                    }
                                    panel.repaint();
                                    break;
                                case "Scribble":
                                    cell.addDrawnPixel(msg.getPixel().x % 50, msg.getPixel().y % 50, msg.getPlayerColor());
                                    panel.repaint();
                                    break;
                                case "FilledClient":
                                    cell.setBeingClaimed(false);
                                    cell.setClaimed(true, msg.getPlayerColor());
                                    panel.repaint();
                                    break;
                                case "UnlockClient":
                                    cell.setBeingClaimed(false);
                                    cell.clearDrawing();
                                    panel.repaint();
                                    break;
                                case "GameOver":
                                    JOptionPane.showMessageDialog(panel, WelcomePanel.getColorName(msg.getPlayerColor()) + " wins the game!");
                                case "Draw":
                                    JOptionPane.showMessageDialog(panel, "Game tied!");
                            }
                            panel.repaint();
                        }
                    });
                }
            }
        } catch (EOFException e) {
            System.err.println("Server closed the connection");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error in server communication: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            // Ignore
        }
    }
}