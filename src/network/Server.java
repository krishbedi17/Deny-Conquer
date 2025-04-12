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
    private final Map<String, String> cellLocks = new HashMap<>(); // Map of cell locks (key: "row,col", value: player ID)

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

    public synchronized boolean lockCell(int row, int col, String playerId) {
        String key = row + "," + col;
        if (cellLocks.containsKey(key)) {
            return false; // Cell is already locked
        }
        cellLocks.put(key, playerId);
        return true;
    }

    public synchronized void unlockCell(int row, int col, String playerId) {
        String key = row + "," + col;
        if (cellLocks.get(key).equals(playerId)) {
            cellLocks.remove(key);
        }
    }

    public void broadcast(MessageToSend message) {
        for (ClientHandler client : clients) {
            client.send(message);
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