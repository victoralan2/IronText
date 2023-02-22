package org.example;



import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.*;
public class Server {

    private ServerSocket serverSocket;

    private final String host;

    private final String pepper = "";
    private final int port;
    private final SQL sqlDB = new SQL();
    private HashMap<UUID, Socket> connections = new HashMap();

    public Server(String host, int port){
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
                            int authType = input.readInt();

                            // 0 = token logging | 1 = email-password logging | 3 = register-account
                            if (authType == 0){

                                String token = input.readUTF();
                                token = token.replace("\"", "").replace("'", "");
                                if(token.length() != 32){
                                    throw new Exception();
                                }

                                PreparedStatement uuidFromTokenPS = sqlDB.prepareStatement("SELECT uuid FROM users WHERE current_token = ?;");
                                uuidFromTokenPS.setString(1, token);

                                ResultSet resultSet = uuidFromTokenPS.executeQuery();

                                PreparedStatement tokenExpireDateFromTokenPS = sqlDB.prepareStatement("SELECT token_expire_date FROM users WHERE current_token = ?;");
                                tokenExpireDateFromTokenPS.setString(1, token);

                                ResultSet resultSet2 = tokenExpireDateFromTokenPS.executeQuery();

                                try {
                                    resultSet2.next();
                                    resultSet.next();

                                    Timestamp expireTime = resultSet2.getTimestamp(1);
                                    if (expireTime.getTime() < System.currentTimeMillis()){
                                        System.out.println("token expried");
                                        PreparedStatement setCurrentTokenToNullPS = sqlDB.prepareStatement("UPDATE users SET current_token = NULL WHERE current_token = ?;");
                                        tokenExpireDateFromTokenPS.setString(1, token);
                                        setCurrentTokenToNullPS.executeQuery();
                                        throw new Exception();
                                    }
                                    if (resultSet.getString(1) != null){
                                        System.out.println("Logged in using token");
                                    } else throw new Exception();
                                } catch (Exception e){
                                    //token not valid
                                }
                            } else if (authType == 1){
                                //password logging
                                Hasher sha256Hasher = new Hasher("SHA256");
                                Hasher bcryptHasher = new Hasher("bcrypt");

                                String email = input.readUTF();
                                String password = input.readUTF();
                                email = email.replace("\"", "").replace("'", "");
                                PreparedStatement saltPS = sqlDB.prepareStatement("SELECT password_salt FROM users WHERE email = ?;");
                                saltPS.setString(1, email);
                                saltPS.executeQuery().next();
                                ResultSet saltRS = saltPS.executeQuery();
                                saltRS.next();
                                String salt = saltRS.getString(1);
                                System.out.println(salt);
                                String encryptedPassword = AES256.encryptAES256(pepper, bcryptHasher.hashString(sha256Hasher.hashString(password), salt), salt);;
                                System.out.println(encryptedPassword);


                                PreparedStatement hashedPasswordFromEmailPS = sqlDB.prepareStatement("SELECT hashed_password FROM users WHERE email = ?;");
                                hashedPasswordFromEmailPS.setString(1, email);
                                ResultSet databasePasswordCheckResult =  hashedPasswordFromEmailPS.executeQuery();
                                databasePasswordCheckResult.next();
                                String databasePassword = databasePasswordCheckResult.getString(1);
                                System.out.println("DB pass: " + databasePassword);

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
                                PreparedStatement usernameRepeatCheckPS = sqlDB.prepareStatement("SELECT uuid FROM users WHERE username = ?;");

                                emailRepeatCheckPS.setString(1, email);
                                usernameRepeatCheckPS.setString(1, username);

                                if (!email.matches(emailRegex) || email.length() > 254 || password.length() > 64 || !password.matches(passwordRegex) || username.length() > 16 || username.contains("'") || username.contains("\"") ) { throw new Exception(); }
                                if (emailRepeatCheckPS.executeQuery().next()
                                || usernameRepeatCheckPS.executeQuery().next()) {
                                    throw new Exception();
                                }


                                String salt = Hasher.randomString(32);
                                String encryptedPassword = AES256.encryptAES256(pepper, bcryptHasher.hashString(sha256Hasher.hashString(password), salt), salt);
                                System.out.println(encryptedPassword);
                                String token = Hasher.randomString(32);
                                Timestamp date = new Timestamp(System.currentTimeMillis() + 1000L * 30);

                                PreparedStatement insertValuesPS = sqlDB.prepareStatement("INSERT INTO users VALUES(?, ?, ?, ?, ?, ?, ?);");
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
                            e.printStackTrace();
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