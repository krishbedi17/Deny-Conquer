package network;
import game.Cell;
import game.GamePanel;
import game.WelcomePanel;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String clientID;

    private final Object lockWaiter = new Object();
    private volatile boolean lockGranted = false;

    GamePanel panel;

    public Client(GamePanel panel) throws IOException {
        this.panel = panel;
        this.socket = new Socket("127.0.0.1", 53333);
        this.clientID = UUID.randomUUID().toString();
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush(); // Ensure the stream header is sent
        this.in = new ObjectInputStream(socket.getInputStream());

        // Optional: listen for server messages in another thread
        new Thread(this::listenToServer).start();
    }

    public synchronized void sendMessage(MessageToSend msg) {
        try {
            msg.setSenderID(clientID);
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
                    if (msg.getType().equals("LockGranted") || msg.getType().equals("LockNotGranted")) {
                        synchronized (lockWaiter) {
                            lockGranted = msg.getType().equals("LockGranted");
                            lockWaiter.notify();
                        }
                    }

                    SwingUtilities.invokeLater(() -> {
                        Cell cell = panel.getCell(msg.row, msg.col);
                        if (cell != null) {
                            switch (msg.getType()) {
                                case "LockGranted":
                                    cell.setBeingClaimed(true);
                                    break;

                                case "LockNotGranted":
                                    cell.setBeingClaimed(false);
                                    cell.clearDrawing();
                                    break;

                                case "Scribble":
                                    cell.addDrawnPixel(msg.getPixel().x % 50, msg.getPixel().y % 50, msg.getPlayerColor());
                                    break;

                                case "Filled":
                                    cell.setBeingClaimed(false);
                                    cell.setClaimed(true, msg.getPlayerColor());
                                    break;

//                                case "Unlock":
//                                    cell.setBeingClaimed(false);
//                                    cell.clearDrawing();
//                                    break;
                                case "NotFilled":
                                    cell.setBeingClaimed(false);
                                    cell.clearDrawing();
                                    break;
                                case "GameOver":
                                    JOptionPane.showMessageDialog(panel, WelcomePanel.getColorName(msg.getPlayerColor()) + " wins the game!");
                                    break;

                                case "Draw":
                                    JOptionPane.showMessageDialog(panel, "Game tied!");
                                    break;
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

    public boolean requestLockAndWait(int row, int col, Point pixel) {
        synchronized (lockWaiter) {
            lockGranted = false;
            MessageToSend requestMsg = new MessageToSend(row, col, pixel, this.panel.getColor(), "RequestLock");
            sendMessage(requestMsg);

            long start = System.currentTimeMillis();
            try {
                while (!lockGranted && (System.currentTimeMillis() - start) < 1000) {
                    lockWaiter.wait(1000); // Wait up to 1 second
                }
            } catch (InterruptedException e) {
                System.err.println("Lock wait interrupted: " + e.getMessage());
            }

            long elapsed = System.currentTimeMillis() - start;
            if (elapsed >= 1000) {
                System.out.println("⚠️ Timeout waiting for lock on cell (" + row + ", " + col + ")");
            }

            return lockGranted;
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