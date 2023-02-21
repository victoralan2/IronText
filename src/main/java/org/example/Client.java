package org.example;

import java.io.*;
import java.net.Socket;

public class Client {
    private DataOutputStream output;
    private DataInputStream input;

    private final String host;
    private final int port;

    public Client(String host, int port){
        this.host = host;
        this.port = port;
    }


    public void startConnection() throws IOException {
        Socket clientSocket = new Socket(host, port);
        output = new DataOutputStream(clientSocket.getOutputStream());
        tokenAuth();
    }

    private void tokenAuth() throws IOException {
        output.writeInt(0);
        output.writeUTF("02SXceGHBJIVdNMNkfoZxz50Lq8BzsEf");
    }
    private void passwordAuth() throws IOException {
        output.writeInt(1);
        //email
        output.writeUTF("vicvarcas2a007@gmail.com");
        //password
        output.writeUTF("Proyecto103");
    }
    private void registerAcc()throws IOException {
        output.writeInt(2);
        output.writeUTF("mariogandia");
        //email
        output.writeUTF("mariogandia55@gmail.com");
        //password
        output.writeUTF("ILoveJava55");
    }
    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }
}
