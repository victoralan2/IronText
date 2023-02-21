package org.example;


import java.awt.*;
import java.awt.image.BufferedImage;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.sql.Date;
import java.util.*;
public class Server {

    private ServerSocket serverSocket;

    private final String host;

    private String pepper;
    private final int port;
    private SQL sqlDB;
    private HashMap<UUID, Socket> connections = new HashMap();

    public Server(String host, int port){
        this.sqlDB = new SQL();
        this.pepper = "Pepper";
        this.host = host;
        this.port = port;
    }

    public void startServer() throws Exception{
        System.out.println("SERVER HAS STARTED");
        serverSocket = new ServerSocket(port);
        new Thread( () -> {
            while (true){
                try {

                    Socket clientSocket = serverSocket.accept();
                    System.out.println("A client has joined");

                    clientSocket.setKeepAlive(true);

                    new Thread( () -> {
                        try {

                            clientSocket.setSoTimeout(5000);
                            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
                            Integer authType = input.readInt();

                            // 0 = token logging | 1 = email-password logging | 3 = register-account
                            if (authType == 0){

                                String token = input.readUTF();
                                token = token.replace("\"", "").replace("'", "");
                                if(token.length() != 32){
                                    throw new Exception();
                                }

                                ResultSet resultSet = sqlDB.executeQuery("SELECT uuid FROM users WHERE current_token = \""+ token + "\";");
                                ResultSet resultSet2 = sqlDB.executeQuery("SELECT token_expire_date FROM users WHERE current_token = \""+ token + "\";");

                                try {
                                    resultSet2.next();
                                    resultSet.next();

                                    Timestamp expireTime = resultSet2.getTimestamp(1);
                                    if (expireTime.getTime() < System.currentTimeMillis()){
                                        System.out.println("token expried");
                                        sqlDB.executeUpdate("UPDATE users SET current_token = NULL WHERE current_token = '"+ token +"';");
                                        throw new Exception();
                                    }
                                    if (resultSet.getString(1) != null){
                                        System.out.println("Logged in using token");
                                    } else throw new Exception();
                                } catch (Exception e){
                                    //token not valid
                                }
                            } else if (authType == 1){

                                Hasher sha256Hasher = new Hasher("SHA256");
                                Hasher bcryptHasher = new Hasher("bcrypt");

                                String email = input.readUTF();
                                String password = input.readUTF();
                                email = email.replace("\"", "").replace("'", "");
                                String salt = sqlDB.executeQueryString("SELECT password_salt FROM users WHERE email = \"" + email + "\"");

                                String encryptedPassword = AES256.encryptAES256(pepper, bcryptHasher.hashString(sha256Hasher.hashString(password), salt), salt);


                                ResultSet databasePasswordResult = sqlDB.executeQuery("SELECT hashed_password FROM users WHERE email = \"" + email + "\"");
                                databasePasswordResult.next();
                                String databasePassword = databasePasswordResult.getString(1);


                                if (Objects.equals(databasePassword, encryptedPassword)){
                                    System.out.println("password's right!");
                                } else {System.out.println("password was not right!");}

                            } else {
                                // Registration
                                Hasher sha256Hasher = new Hasher("SHA256");
                                Hasher bcryptHasher = new Hasher("bcrypt");

                                String emailRegex = "^[a-zA-Z0-9_!#$%&'*+\\=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
                                // Minimum eight characters, at least one uppercase letter, one lowercase letter and one number
                                String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z_\\\\\\/\\-\\d]{8,}$";
                                String username = input.readUTF();
                                String email = input.readUTF();
                                String password = input.readUTF();
                                PreparedStatement emailRepeatCheckPS = sqlDB.prepareStatement("SELECT uuid FROM users WHERE email = ?;");

                                if (!email.matches(emailRegex) || email.length() > 254 || password.length() > 64 || !password.matches(passwordRegex) || username.length() > 16 || username.contains("'") || username.contains("\"") ) { throw new Exception(); }
                                if (sqlDB.executeQuery("SELECT uuid FROM users WHERE email = '" + email + "';").next()
                                || sqlDB.executeQuery("SELECT uuid FROM users WHERE username = '" + username + "';").next()) {
                                    throw new Exception();
                                }


                                String salt = Hasher.randomString(32);
                                String encryptedPassword = AES256.encryptAES256(pepper, bcryptHasher.hashString(sha256Hasher.hashString(password), salt), salt);
                                String token = Hasher.randomString(32);
                                Timestamp date = new Timestamp(System.currentTimeMillis() + 1000L * 30);

                                //sqlDB.executeUpdate("INSERT INTO users VALUES('"+UUID.randomUUID()+"','" +username+ "','" + email + "','"+encryptedPassword+"','"+ token + "','" + date +"','" +salt + "');");
                                PreparedStatement insertValuesPS = sqlDB.prepareStatement("INSERT INTO users VALUES(?,?,?,?,?,?,?);");
                                insertValuesPS.setString(1, UUID.randomUUID().toString());
                                insertValuesPS.setString(2, username);
                                insertValuesPS.setString(3, email);
                                insertValuesPS.setString(4, encryptedPassword);
                                insertValuesPS.setString(5, token);
                                insertValuesPS.setTimestamp(6, date);
                                insertValuesPS.setString(7, salt);

                                insertValuesPS.executeUpdate();
                                System.out.println("Registered a new account with the username: "+ username);

                            }
                        } catch (Exception e) {
                            try {
                                clientSocket.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }).start();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        }).start();


    }


    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }
}