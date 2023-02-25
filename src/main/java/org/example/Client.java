package org.example;

import com.mysql.cj.Messages;
import com.mysql.cj.xdevapi.DatabaseObject;

import java.awt.image.DataBufferInt;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.UUID;

public class Client {
    private DataOutputStream output;
    private DataInputStream input;

    private final String host;
    private final int port;
    private Socket socket;

    public Client(String host, int port){
        this.host = host;
        this.port = port;
    }


    public void startConnection() throws Exception {
        Socket socket = new Socket(host, port);
        this.socket = socket;
        output = new DataOutputStream(socket.getOutputStream());
        input = new DataInputStream(socket.getInputStream());
        tokenAuth();
    }

    private void tokenAuth() throws Exception {
        output.writeInt(0);
        output.writeUTF("YwvFtgñNznfU5gjjCc59OijZ9qWxZ303");
        int resultCode = input.readInt();
        System.out.println("EXIT CODE: " + resultCode);
        if (resultCode == 0){
            awaitForPacket();
            Thread.sleep(1000);
            sendEvent(0, socket, "messages");
        }
    }
    private void passwordAuth() throws Exception {
        output.writeInt(1);
        //email
        output.writeUTF("vicvarcas2007@gmail.com");
        //password
        output.writeUTF("Proyecto103");
        int resultCode = input.readInt();
        System.out.println(resultCode);

        if (resultCode == 0){
            String newToken = input.readUTF();

            awaitForPacket();
            Thread.sleep(1000);
            sendEvent(0, socket, "message");
        }

    }
    private void registerAcc() throws Exception {
        output.writeInt(2);
        output.writeUTF("thedracon");
        //email
        output.writeUTF("vicvarcas2007@gmail.com");
        //password
        output.writeUTF("Proyecto103");
        int resultCode = input.readInt();
        System.out.println(resultCode);

        if (resultCode == 0){

        }

    }
    private void awaitForPacket(){
        new Thread(() ->{
            while (true){
                if (socket.isClosed()) break;
                try {
                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    int requestType = input.readInt();
                    if (requestType == 0){
                        String message = input.readUTF();
                        String sender = input.readUTF();
                        long timestamp = input.readLong();
                        System.out.println(message);
                        System.out.println(sender);
                        System.out.println(timestamp);

                        // Do something

                    } else if (requestType == 1){
                        int amountOfRows = input.readInt();
                        requestType = input.readInt();
                        ArrayList<Message> messages = new ArrayList<>();
                        for (int i = 0; i < amountOfRows; i++) {
                            String content = input.readUTF();
                            String sender = input.readUTF();
                            long timestamp = input.readLong();
                            Message currentMessage = new Message(content, sender, timestamp);
                            messages.add(currentMessage);
                        }
                        for (Message message : messages){
                            System.out.println(message.getContent());

                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public void sendEvent(int requestType, Socket serverSocket, Object... dataList)  {
        try {
            DataOutputStream output = new DataOutputStream(serverSocket.getOutputStream());
            output.writeInt(requestType);
            for (Object data : dataList){
                if (data instanceof String || data instanceof UUID){
                    output.writeUTF(data.toString());
                } else if (data instanceof Integer) {
                    output.writeInt(((Integer) data));
                } else if (data instanceof Long) {
                    output.writeFloat(((Long) data));
                } else if (data instanceof Boolean) {
                    output.writeBoolean(((Boolean) data));
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }
}
