package server;

import game.*;
import Client.MessageToSend;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class server {
    private ServerSocket serverSocket;
    private final ArrayList<clientHandler> clients = new ArrayList<>();
    GameBoard board;
    private boolean[][] cellLockArray = new boolean[8][8];

    public server() throws IOException {
        serverSocket = new ServerSocket(53333);
        board = new GameBoard();
        System.out.println("Server started on port 53333");
    }

    public GameBoard getBoard() {
        return board;
    }
    public boolean getCellLock(int row,int col){
        return cellLockArray[row][col];
    }

    public void setCellLock(int row,int col,boolean bool){
        cellLockArray[row][col] = bool;
    }

    public void setGameBoardCell(int row, int col,Cell cell){
        board.setGameBoardElem(row,col,cell);
    }

    public void start() throws IOException {
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("New client connected: " + socket.getInetAddress());

            clientHandler handler = new clientHandler(socket, this);  // Pass server reference if needed
            clients.add(handler);
            new Thread(handler).start();
        }
    }

    public void broadcast(MessageToSend message) {
        for (clientHandler client : clients) {
            client.send(message);
        }
    }
    public void sendToClient(String clientID, MessageToSend msg) {
        for (clientHandler client : clients) {
            if (client.getClientId().equals(clientID)) {
                client.send(msg);
                break;
            }
        }
    }
    public void remove(clientHandler client) {
        clients.remove(client);
    }

    public static void main(String[] args) {
        try {
            server server = new server();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}