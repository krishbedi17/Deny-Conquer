package network;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    ObjectInputStream in = null;
    ObjectOutputStream out = null;
    private final Server server;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public void send(Object obj) {
        try {
            if (out != null) {
                out.writeObject(obj);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            Object obj;
            while ((obj = in.readObject()) != null) {
                if (obj instanceof MessageToSend message) {
                    System.out.println("Received message: " + message);

                    if (message.getType().equals("Lock")) {
                        boolean success = server.lockCell(message.getRow(), message.getCol(), socket.getInetAddress().toString());
                        if (!success) {
                            // Notify the client that the cell is already locked
                            MessageToSend rejectMsg = new MessageToSend(message.getRow(), message.getCol(), null, message.getPlayerColor(), "Reject");
                            send(rejectMsg);
                            continue;
                        }
                    } else if (message.getType().equals("Unlock") || message.getType().equals("Filled")) {
                        // Unlock the cell when the player releases or claims it
                        server.unlockCell(message.getRow(), message.getCol(), socket.getInetAddress().toString());
                    }

                    server.broadcast(message); // Broadcast the message to all clients
                }
            }
        } catch (EOFException e) {
            // Client closed connection
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
            server.remove(this);
            System.out.println("Client disconnected: " + socket.getInetAddress());
        }
    }
}
