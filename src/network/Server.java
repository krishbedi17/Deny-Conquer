package network;

import game.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private ServerSocket serverSocket;
    private final ArrayList<ClientHandler> clients = new ArrayList<>();
    private final GameBoard board;
    private final boolean[][] lockedCells = new boolean[4][4]; // match your board size (4x4)


    public Server() throws IOException {
        serverSocket = new ServerSocket(53333);
        board = new GameBoard();
        System.out.println("Server started on port 53333");
    }

    public void start() throws IOException {
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("New client connected: " + socket.getInetAddress());

            ClientHandler handler = new ClientHandler(socket, this);
            clients.add(handler);
            new Thread(handler).start();
        }
    }

    public synchronized void broadcast(MessageToSend message) {
        int row = message.getRow();
        int col = message.getCol();

        switch (message.getType()) {
            case "Lock":
                if (!lockedCells[row][col]) {
                    lockedCells[row][col] = true;
                    for (ClientHandler client : clients) {
                        client.send(message);
                    }
                } // else: ignore or notify requesting client it failed to lock
                break;

            case "Unlock":
            case "Filled":
                lockedCells[row][col] = false;
                for (ClientHandler client : clients) {
                    client.send(message);
                }
                break;

            default:
                for (ClientHandler client : clients) {
                    client.send(message);
                }
                break;
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
}