package org.irontext;


public class Main {
    public static void main(String[] args) {
        try {
            Server server = new Server("localhost", 1252);
            server.startServer();
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
