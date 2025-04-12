package server;

import Client.MessageToSend;
import game.Cell;
import game.GameBoard;
import game.WelcomePanel;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static game.WelcomePanel.getColorFromName;
import static game.WelcomePanel.getColorName;

public class clientHandler implements Runnable {
    private final Socket socket;
    ObjectInputStream in = null;
    ObjectOutputStream out = null;
    private final server server;
    private String clientId = null;
    public clientHandler(Socket socket, server server) {
        this.socket = socket;
        this.server = server;
    }

    public String getClientId() {
        return clientId;
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
    private void handleRequestMsg(int row, int col,MessageToSend msg){
        boolean cell = server.getCellLock(row, col);
        MessageToSend reply = new MessageToSend(msg.getRow(), msg.getCol(),
                msg.getPixel(),msg.getPlayerColor(),"",msg.getClientID());
        if(!cell){//if cell is not locked
            server.setCellLock(row,col,true);
            //give access to client
           reply.setType("LockGranted");
        }
        else{
            //if cell is locked
            //deny access to client
            reply.setType("LockDenied");
        }
        server.sendToClient(reply.getClientID(),reply);
    }

    private void handleUnlockMsg(int row,int col,MessageToSend msg){
        server.setCellLock(row,col,false);
        msg.setType("UnlockClient");
        server.broadcast(msg);
//        server.sendToClient(msg.getClientID(),
    }

    private void handleFilledMsg(int row,int col,MessageToSend msg){
        Point point = new Point(msg.getRow()*50, msg.getCol()*50);
        Cell cell = new Cell(point.x, point.y);
        cell.setClaimed(true,msg.getPlayerColor());
        server.setGameBoardCell(row,col,cell);
        msg.setType("FilledClient"); // ensure the type is set correctly
        server.broadcast(msg); // <---- BROADCAST THE FILLED CELL
    }


    public String checkWinCondition() {
        GameBoard board = server.getBoard();
        HashMap<String, Integer> colorCounts = new HashMap<>();
        for (int row = 0; row < board.getBoardSize(); row++) {
            for (int col = 0; col < board.getBoardSize(); col++) {
                Cell cell = board.getCellByRowAndCol(row, col);
                Color color = cell.getColorOfCell();
                String colorName = WelcomePanel.getColorName(color);
                if (colorName.equalsIgnoreCase("WHITE")) {
                    return null;
                }
                colorCounts.put(colorName, colorCounts.getOrDefault(colorName, 0) + 1);
            }
        }

        String winner = null;
        int maxCount = 0;
        boolean tie = false;

        for (Map.Entry<String, Integer> entry : colorCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                winner = entry.getKey();
                maxCount = entry.getValue();
                tie = false;
            } else if (entry.getValue() == maxCount) {
                tie = true; // Found a tie
            }
        }

        if (tie) {
            return "Draw";
        }
        return winner;
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
//                    System.out.println(message.pixel.x + message.pixel.y);
                    out.writeObject("Received your message!");
//                    server.broadcast(message);
                    if(this.clientId == null){
                        this.clientId = message.getClientID();
                    }
                    switch (message.getType()) {
                        case "RequestLock":
                            handleRequestMsg(message.getRow(), message.getCol(),message);
                            break;
                        case "Unlock":
                            handleUnlockMsg(message.getRow(), message.getCol(),message);
                            break;
                        case "Filled":
                            handleFilledMsg(message.getRow(), message.getCol(),message);
                            String color = checkWinCondition();

                            if (color != null && color.equals("Draw")) {
                                MessageToSend winMsg = new MessageToSend(0, 0, new Point(0, 0), Color.WHITE, "Draw", message.getClientID());
                                server.broadcast(winMsg);
                                return;
                            }

                            if (color != null) {
                                Color winnerColor = WelcomePanel.getColorFromName(color);
                                MessageToSend winMsg = new MessageToSend(0, 0, new Point(0, 0), winnerColor, "GameOver", message.getClientID());
                                server.broadcast(winMsg);
                            }
                            break;
                        case "Scribble":
                            server.broadcast(message);
                            break;
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
}