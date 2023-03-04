package org.irontext;


public class Main {
    public static Server server;
    public static void main(String[] args) {
        try {
            server = new Server("localhost", 1252);
            server.startServer();
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
