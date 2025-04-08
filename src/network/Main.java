//package network;
//
//import java.io.IOException;
//
////TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
//// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
//public class Main {
//    public static void main(String[] args) throws IOException {
//        Server server = new Server();
//        new Thread(() -> {
//            try { Server.main(null); } catch (IOException e) { e.printStackTrace(); }
//        }).start();
//        try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
//        new Thread(() -> {
//            try { Client.main(null); } catch (IOException e) { e.printStackTrace(); }
//        }).start();
////        server.close();
////        server1.close();
//    }
//}