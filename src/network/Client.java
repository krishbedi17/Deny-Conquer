package network;
import game.Cell;
import game.GamePanel;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.UUID;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String clientID;
    GamePanel panel;

    private final Object lockWaiter = new Object();
    private volatile boolean lockGranted = false;
    public Client(GamePanel panel) throws IOException {
        this.panel = panel;
        this.socket = new Socket("127.0.0.1", 53333);
        this.clientID = UUID.randomUUID().toString();
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());

        // Optional: listen for server messages in another thread
        new Thread(this::listenToServer).start();
    }

    public void sendMessage(MessageToSend msg) {
        try {
            msg.setSenderID(clientID);
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenToServer() {
        try {
            while (true) {
                Object response = in.readObject();
//                System.out.println("Server: " + response);

                if (response instanceof MessageToSend msg) {
                    System.out.println("Received: " + msg.getType());

//                    panel.drawPixel(panel.getCell(msg.col, msg.row), msg.pixel.x, msg.pixel.y ,msg.getPlayerColor());
                    if (msg.getType().equals("Scribble")) {
                        SwingUtilities.invokeLater(() -> {
                            Cell cell = panel.getCell(msg.row, msg.col);
                            if (cell != null) {
//                                System.out.println("Cell is not null");
                                cell.setBeingClaimed(true);
                                cell.addDrawnPixel(msg.pixel.x % 50, msg.pixel.y % 50, msg.getPlayerColor());
                                panel.repaint();
                            }
                        });
                    } else if (msg.getType().equals("ReleaseFilled") || msg.getType().equals("ReleaseNotFilled") ) {
                        SwingUtilities.invokeLater(() -> {
                            Cell cell = panel.getCell(msg.row, msg.col);
                            if (cell != null) {
                                cell.checkIfValidFill(msg.getPlayerColor());
                                panel.repaint();
                            }
                        });
                    }
                    else if (msg.getType().equals("LockGranted")) {
                        synchronized (lockWaiter) {
                            lockGranted = true;
                            lockWaiter.notify();
                        }
                    } else if (msg.getType().equals("LockDenied")) {
                        synchronized (lockWaiter) {
                            lockGranted = false;
                            lockWaiter.notify();
                        }
                    }
                }

            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Server connection closed or error: " + e.getMessage());
        }
    }

    public boolean requestLockAndWait(int row, int col, Point pixel) {
        synchronized (lockWaiter) {
            lockGranted = false;
            MessageToSend requestMsg = new MessageToSend(row, col, pixel, Color.BLACK, "RequestLock", this.clientID);
            sendMessage(requestMsg);

            long start = System.currentTimeMillis();
            try {
                lockWaiter.wait(1000); // wait up to 1 second
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

    public boolean getLockGranted() {
        return this.lockGranted;
    }


    public void setLockGranted(boolean b) {
        lockGranted = b;
    }

    public String getClientID() {
        return clientID;
    }
}
