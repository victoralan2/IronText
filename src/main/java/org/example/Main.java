package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server("localhost", 2918);
        server.startServer();
        Client client = new Client("localhost", 2918);
        client.startConnection();
    }
}