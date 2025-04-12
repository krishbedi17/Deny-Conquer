package network;

import game.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private ServerSocket serverSocket;
    private final ArrayList<ClientHandler> clients = new ArrayList<>(); // changed this form something might break
    GameBoard board;

    public Server() throws IOException {
        serverSocket = new ServerSocket(53333);
        board = new GameBoard();
        System.out.println("Server started on port 53333");
    }

    public void start() throws IOException {
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("New client connected: " + socket.getInetAddress());

            ClientHandler handler = new ClientHandler(socket, this);  // Pass server reference if needed
            clients.add(handler);
            new Thread(handler).start();
        }
    }

    public void broadcast(MessageToSend message) {
        for (ClientHandler client : clients) {
            client.send(message);
        }
    }

    public void sendToClient(String clientID, MessageToSend msg) {
        for (ClientHandler client : clients) {
            if (client.getClientID().equals(clientID)) {
                client.send(msg);
                break;
            }
        }
    }

//    public void serverAction(MessageToSend message) {
//        String messageType = message.getType();
//        System.out.println("Message Type: " + messageType);
//        if (messageType.equals("RequestLock")) {
//            grantLock(message);
//        } else if (messageType.equals("ReleaseFilled")) {
//            // stays locked
//            broadcast(message);
//        }  else if (messageType.equals("ReleaseNotFilled")) {
//            Cell releasedCell = board.getCellByRowAndCol(message.getRow(), message.getCol());
//            if (releasedCell != null) {
//                releasedCell.unlock();
//            }
//            broadcast(message);
//        } else if (messageType.equals("Scribble")) {
//            broadcast(message);
//        }
//    }

    public void grantLock(MessageToSend message) {
        Cell requestedCell = board.getCellByRowAndCol(message.getRow(), message.getCol());
        System.out.println("Lock requested by" + message.getSenderID());
        if (requestedCell != null && requestedCell.tryLock()) {
            // Lock acquired — send confirmation only to sender
            message.setType("LockGranted");
            sendToClient(message.getSenderID(), message);
            System.out.println("Lock was granted to: " + message.getSenderID());
        } else {
            // Lock not acquired — deny request
            System.out.println("DENIED: " + message.senderID);
            message.setType("LockNotGranted");
            sendToClient(message.getSenderID(), message);
            System.out.println("Lock not able granted to: " + message.getSenderID());
        }
    }

    public void remove(ClientHandler client) {
        clients.remove(client);
    }

    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unlockCell(int row, int col) {
        board.getCellByRowAndCol(row, col).unlock();
    }
}
