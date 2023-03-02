package org.irontext;

import com.mysql.cj.Messages;
import com.mysql.cj.xdevapi.DatabaseObject;

import java.awt.image.DataBufferInt;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Client {
    private DataOutputStream output;
    private DataInputStream input;

    private final String host;
    private final int port;
    private Socket socket;
    private int i;

    public Client(String host, int port){
        this.host = host;
        this.port = port;
    }

    public void startConnection(int i) throws Exception {
        Socket socket = new Socket(host, port);
        this.socket = socket;
        output = new DataOutputStream(socket.getOutputStream());
        input = new DataInputStream(socket.getInputStream());
        this.i = i+1;
        if (i == 1){
            Thread.sleep(0);
            tokenAuth();
        } else if (i == 2){
            Thread.sleep(100);
            tokenAuth1();
        } else if (i == 3) {
            Thread.sleep(4000);
            passwordAuth();
        }
    }

    private void tokenAuth() throws Exception {
        output.writeInt(0);
        output.writeUTF("5V71I1LDi0yñI2ñSjYDkOdhNJBui6Git");
        int resultCode = input.readInt();
        System.out.println("EXIT CODE: " + resultCode);
        if (resultCode == 0){
            listenForPackets();
            Thread.sleep(500);
            System.out.println("sending request");
            sendEvent(0, socket, "Message from 0");
            sendEvent(0, socket, "Message from 0 but twice");
            sendEvent(0, socket, "Message from 0 and 3 times one");

        }
    }
    private void tokenAuth1() throws Exception {
        output.writeInt(0);
        output.writeUTF("QdDXLUemeb5FJZYuinAmCIFjdtpw1dr4");
        int resultCode = input.readInt();
        System.out.println("EXIT CODE: " + resultCode);
        if (resultCode == 0){
            listenForPackets();
            Thread.sleep(2000);
            System.out.println("sending request");
            sendEvent(0, socket, "Message from 1");
        }
    }
    private void tokenAuth2() throws Exception {
        output.writeInt(0);
        output.writeUTF("XABqlwZoKY2KiCab8RvY3ujl4Eñhs6c5");
        int resultCode = input.readInt();
        System.out.println("EXIT CODE: " + resultCode);
        if (resultCode == 0){
            listenForPackets();
            Thread.sleep(4000);
            System.out.println("sending request");
            sendEvent(0, socket, "Messa3 asdge fr2 om 211as x2");
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

            listenForPackets();
            Thread.sleep(1000);
            sendEvent(1, socket, 50);
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
    private void registerAcc1() throws Exception {
        output.writeInt(2);
        output.writeUTF("mariogandia");
        //email
        output.writeUTF("mario@gmail.com");
        //password
        output.writeUTF("Password1");
        int resultCode = input.readInt();
        System.out.println(resultCode);

        if (resultCode == 0){

        }

    }
    private void registerAcc2() throws Exception {
        output.writeInt(2);
        output.writeUTF("cargandoc");
        //email
        output.writeUTF("cargandoc@gmail.com");
        //password
        output.writeUTF("Password2");
        int resultCode = input.readInt();
        System.out.println(resultCode);

        if (resultCode == 0){

        }
    }


    private void listenForPackets(){
        new Thread(() ->{
            while (true){
                if (socket.isClosed()) break;
                try {
                    DataInputStream input = new DataInputStream(socket.getInputStream());

                    // 0 = one new message | 1 = x new messages
                    int requestType = input.readInt();
                    if (requestType == 0){
                        String message = input.readUTF();
                        String sender = input.readUTF();
                        long timestamp = input.readLong();
                        Message newMessage = new Message(message, sender, timestamp);

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String formattedDate = sdf.format(new Date(newMessage.getTimestamp()));
                        System.out.println(i + " MESSAGE: '" +newMessage.getContent() +"' BY: '" + newMessage.getSender() + "' ON: " + formattedDate);



                        // Do something

                    } else if (requestType == 1){
                        int amountOfRows = input.readInt();
                        ArrayList<Message> messages = new ArrayList<>();
                        for (int i = 0; i < amountOfRows; i++) {
                            String content = input.readUTF();
                            String sender = input.readUTF();
                            long timestamp = input.readLong();
                            Message currentMessage = new Message(content, sender, timestamp);
                            messages.add(currentMessage);
                        }
                        for (Message message : messages){
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String formattedDate = sdf.format(new Date(message.getTimestamp()));
                            System.out.println("MESSAGE: '" +message.getContent() +"' BY: '" + message.getSender() + "' ON: " + formattedDate);
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
