package org.example;

public class Main {
    public static void main(String[] args) throws Exception {


        try {
            Server server = new Server("localhost", 2918);
            new Thread(() ->{
                try {
                    server.startServer();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }).start();


            Client client2 = new Client("localhost", 2918);
            client2.startConnection();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}