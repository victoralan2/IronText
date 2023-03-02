package org.irontext;


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


            Client client = new Client("localhost", 2918);
            new Thread(()->{
                try {
                    client.startConnection(1);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
            Client client2 = new Client("localhost", 2918);
            new Thread(()->{
                try {
                    client2.startConnection(2);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
            Client client3 = new Client("localhost", 2918);
            new Thread(()->{
                try {
                    client3.startConnection(3);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
