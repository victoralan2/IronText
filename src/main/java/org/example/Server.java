package org.example;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class Server {

    public ServerSocket serverSocket;

    public final String host;
    public final EventManager eventManager = new EventManager();
    private final String pepper = "d88185c247d598612028529c71395a583638a3bec4eca632da0969177d78a30c";
    public final int port;
    public final SQL sqlDB = new SQL();
    public static final HashMap<UUID, Socket> connections = new HashMap<>();

    public Server(String host, int port){
        this.host = host;
        this.port = port;
    }

    public void startServer() throws Exception{
        System.out.println("SERVER HAS STARTED");
        serverSocket = new ServerSocket(port);


        while (true){
            try {

                Socket clientSocket = serverSocket.accept();
                System.out.println("A client has joined");

                clientSocket.setKeepAlive(true);
                new Thread( () -> {
                    try {
                        DataInputStream input = new DataInputStream(clientSocket.getInputStream());
                        DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
                        int authType = input.readInt();

                        // 0 = token logging | 1 = email-password logging | 3 = register-account

                        if (authType == 0){
                            System.out.println("CLINT SELECTED TOKEN AUTH");
                            String token = input.readUTF();
                            if(token.length() != 32) {
                                output.writeInt(AuthExitCodes.TOKEN_NOT_VALID);
                                clientSocket.close();
                                return;
                            }
                            PreparedStatement dataFromTokenPS = sqlDB.prepareStatement("SELECT * FROM users WHERE current_token = ?;");
                            dataFromTokenPS.setString(1, token);

                            ResultSet data = dataFromTokenPS.executeQuery();



                                if (data.next()){
                                    Timestamp expireTime = data.getTimestamp("token_expire_date");
                                    if (expireTime.getTime() < System.currentTimeMillis()){
                                        System.out.println("token expired");
                                        PreparedStatement setCurrentTokenToNullPS = sqlDB.prepareStatement("UPDATE users SET current_token = NULL WHERE current_token = ?;");
                                        setCurrentTokenToNullPS.setString(1, token);
                                        setCurrentTokenToNullPS.executeUpdate();


                                        output.writeInt(AuthExitCodes.TOKEN_EXPIRED_ERROR);
                                        clientSocket.close();
                                        return;
                                    } else {
                                        // Logged in with token
                                        connections.put(UUID.fromString(data.getString("uuid")), clientSocket);

                                        output.writeInt(AuthExitCodes.SUCCESS);

                                        eventManager.subscribe(clientSocket);
                                        PacketManager packetManager = new PacketManager(this.serverSocket, sqlDB, clientSocket, UUID.fromString(data.getString("uuid")), eventManager);
                                        Thread requestHandlerThread = new Thread(packetManager);
                                        requestHandlerThread.start();
                                    }
                                } else {
                                    output.writeInt(AuthExitCodes.TOKEN_NOT_VALID);
                                    clientSocket.close();
                                    return;
                                }


                        } else if (authType == 1){
                            System.out.println("CLINT SELECTED PASSWORD VERIFICATION");
                            //password logging
                            Hasher sha256Hasher = new Hasher("SHA256");
                            Hasher bcryptHasher = new Hasher("bcrypt");

                            // Reading email and password
                            String email = input.readUTF();
                            String password = input.readUTF();

                            // Selecting the password_salt to verify the passworrd
                            PreparedStatement dataPS = sqlDB.prepareStatement("SELECT * FROM users WHERE email = ?;");
                            dataPS.setString(1, email);
                            ResultSet data = dataPS.executeQuery();
                            if (!data.next()){
                                output.writeInt(AuthExitCodes.NON_EXISTING_EMAIL);
                                clientSocket.close();
                                return;
                            }


                            String salt = data.getString("password_salt"); // Password salt
                            String encryptedPassword = AES256.encryptAES256(pepper, bcryptHasher.hashString(sha256Hasher.hashString(password), salt), salt); // The full encrypted password

                            // Getting the actual encrypted password
                            String databasePassword = data.getString("hashed_password");

                            // Checking if password is correct
                            if (Objects.equals(databasePassword, encryptedPassword)){

                                System.out.println("password's right!");
                                // Logged in successfully
                                String newToken = "";
                                if (data.getString("current_token") == null){
                                    newToken = Hasher.randomString(32);
                                    Timestamp date = new Timestamp(System.currentTimeMillis() + 1000L * 60 * 60 * 24);


                                    PreparedStatement updateTokenPS = sqlDB.prepareStatement("UPDATE users SET current_token = ? AND token_expire_date = ? WHERE uuid = ?;");
                                    updateTokenPS.setString(1, newToken);
                                    updateTokenPS.setTimestamp(2, date);
                                    updateTokenPS.setString(3, data.getString("uuid"));
                                }

                                output.writeInt(AuthExitCodes.SUCCESS);
                                output.writeUTF(newToken);

                                connections.put(UUID.fromString(data.getString("uuid")), clientSocket);
                                eventManager.subscribe(clientSocket);
                                PacketManager packetManager = new PacketManager(this.serverSocket, sqlDB, clientSocket, UUID.fromString(data.getString("uuid")), eventManager);
                                Thread requestHandlerThread = new Thread(packetManager);
                                requestHandlerThread.start();

                            } else {
                                output.writeInt(AuthExitCodes.INCORRECT_PASSWORD);
                                clientSocket.close();
                                return;
                            }

                        } else {
                            System.out.println("CLINT WANTS TO CREATE AN ACCOUNT");

                            // Registration
                            Hasher sha256Hasher = new Hasher("SHA256");
                            Hasher bcryptHasher = new Hasher("bcrypt");

                            // Email regex
                            String emailRegex = "^[a-zA-Z0-9_!#$%&'*+\\=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
                            // Minimum eight characters, at least one uppercase letter, one lowercase letter and one number
                            String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z_\\\\\\/\\-\\d]{8,}$";

                            String usernameRegex = "^[a-zA-Z0-9.,_\\-\\/\\\\()!?¿¡' ]{3,16}$";

                            // Read user data
                            String username = input.readUTF();
                            String email = input.readUTF();
                            String password = input.readUTF();

                            // Statements for checking if email or username are taken
                            PreparedStatement emailPS = sqlDB.prepareStatement("SELECT * FROM users WHERE email = ?;");
                            PreparedStatement usernamePS = sqlDB.prepareStatement("SELECT * FROM users WHERE hashed_password = ?;");

                            emailPS.setString(1, email);
                            usernamePS.setString(1, username);
                            ResultSet emailRS = emailPS.executeQuery();
                            ResultSet usernameRS = usernamePS.executeQuery();

                            // Checking requirements
                            if (!email.matches(emailRegex) || email.length()> 254){
                                output.writeInt(AuthExitCodes.INVALID_EMAIL);
                                clientSocket.close();
                                return;
                            }
                            if (emailRS.next()){
                                output.writeInt(AuthExitCodes.EMAIL_TAKEN);
                                clientSocket.close();
                                return;
                            }
                            if (password.length() > 64 || !password.matches(passwordRegex)){
                                output.writeInt(AuthExitCodes.INVALID_PASSWORD);
                                clientSocket.close();
                                return;
                            }
                            if (username.length() > 16 || username.length() < 3 || !username.matches(usernameRegex)) {
                                output.writeInt(AuthExitCodes.INVALID_USERNAME);
                                clientSocket.close();
                                return;
                            }
                            if (usernameRS.next()){
                                output.writeInt(AuthExitCodes.USERNAME_TAKEN);
                                clientSocket.close();
                                return;
                            }

                            // Encrypting the password
                            String salt = Hasher.randomString(32);
                            String encryptedPassword = AES256.encryptAES256(pepper, bcryptHasher.hashString(sha256Hasher.hashString(password), salt), salt);
                            System.out.println(encryptedPassword);
                            String token = Hasher.randomString(32);
                            Timestamp date = new Timestamp(System.currentTimeMillis() + 1000L * 60 * 60 * 24);

                            // Inserting all values to the database
                            PreparedStatement insertValuesPS = sqlDB.prepareStatement("INSERT INTO users VALUES(?, ?, ?, ?, ?, ?, ?);");
                            insertValuesPS.setString(1, UUID.randomUUID().toString());
                            insertValuesPS.setString(2, username);
                            insertValuesPS.setString(3, email);
                            insertValuesPS.setString(4, encryptedPassword);
                            insertValuesPS.setString(5, token);
                            insertValuesPS.setTimestamp(6, date);
                            insertValuesPS.setString(7, salt);

                            insertValuesPS.executeUpdate();
                            output.writeInt(AuthExitCodes.SUCCESS);
                            clientSocket.close();
                        }
                    } catch (Exception e) {
                        try {
                            e.printStackTrace();
                            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
                            output.writeInt(AuthExitCodes.UNKNOWN_ERROR);
                            clientSocket.close();
                            return;
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }

                }).start();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

    }
//    private void verifyEmail(String email) throws MessagingException {
//        // IMPORTANT: PORT FORWARD PORT 25 (maybe), password = Password_123
//        Properties prop = new Properties();
//        prop.put("mail.smtp.auth", true);
//        prop.put("mail.smtp.starttls.enable", "true");
//        prop.put("mail.smtp.host", "smtp.mailtrap.io");
//        prop.put("mail.smtp.port", "25");
//        prop.put("mail.smtp.ssl.trust", "smtp.mailtrap.io");
//        Session session = Session.getInstance(prop, new Authenticator() {
//            @Override
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication("messagingapp.victor@gmail.com", "Password_123");
//            }
//        });
//        Message message = new MimeMessage(session);
//        message.setFrom(new InternetAddress("from@gmail.com"));
//        message.setRecipients(
//                Message.RecipientType.TO, InternetAddress.parse("to@gmail.com"));
//        message.setSubject("Mail Subject");
//
//        String msg = "This is my first email using JavaMailer";
//
//        MimeBodyPart mimeBodyPart = new MimeBodyPart();
//        mimeBodyPart.setContent(msg, "text/html; charset=utf-8");
//
//        Multipart multipart = new MimeMultipart();
//        multipart.addBodyPart(mimeBodyPart);
//
//        message.setContent(multipart);
//
//        Transport.send(message);
//
//
//    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

}
