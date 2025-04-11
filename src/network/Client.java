package network;
import game.Cell;
import game.GamePanel;
import game.WelcomePanel;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    GamePanel panel;

    public Client(GamePanel panel) throws IOException {
        this.panel = panel;
        this.socket = new Socket("127.0.0.1", 53333);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());

        // Optional: listen for server messages in another thread
        new Thread(this::listenToServer).start();
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
                        if (cell != null) {
                            switch (msg.getType()) {
                                case "Lock":
                                    cell.setBeingClaimed(true);
                                    break;

                                case "Scribble":
                                    cell.addDrawnPixel(msg.getPixel().x % 50, msg.getPixel().y % 50, msg.getPlayerColor());
                                    break;

                                case "Filled":
                                    cell.setBeingClaimed(false);
                                    cell.setClaimed(true, msg.getPlayerColor());
                                    break;

                                case "Unlock":
                                    cell.setBeingClaimed(false);
                                    cell.clearDrawing();
                                    break;
                                case "GameOver":
                                    JOptionPane.showMessageDialog(panel, WelcomePanel.getColorName(msg.getPlayerColor()) + " wins the game!");
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