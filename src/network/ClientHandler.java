package network;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    ObjectInputStream in = null;
    ObjectOutputStream out = null;
    private final Server server;
    private String clientId;
    private static int count = 0;


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

                    if (count == 0) {this.clientId = message.getSenderID();}
//                    System.out.println(message.pixel.x + message.pixel.y);
//                    out.writeObject("Received your message!");
                    if (message.getType().equals("RequestLock")) {
                        System.out.println("Received message: " + message.getType());
                        server.grantLock(message);
                    } else if (message.getType().equals("NotFilled")) {
                        server.unlockCell(message.getRow(), message.getCol());
                        server.broadcast(message);
                    }
                    else {
                        server.broadcast(message);
                    }
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
    public String getClientID() {
        return clientId;
    }

}
