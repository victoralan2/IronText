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
        passwordAuth();
    }

    private void tokenAuth() throws IOException {
        output.writeInt(0);
        output.writeUTF("9WWfhd8vnu8yygxcdCRZSStjFemd9psL");
    }
    private void passwordAuth() throws IOException {
        output.writeInt(1);
        //email
        output.writeUTF("mariogandia55@gmail.com");
        //password
        output.writeUTF("ILoveJava55");
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
