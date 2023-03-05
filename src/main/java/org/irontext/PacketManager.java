package org.irontext;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;

public class PacketManager extends Thread{
	private ServerSocket server;
	private Socket clientSocket;
	private SQL sqlDB;
	private UUID clientUUID;
	private EventManager eventManager;


	public PacketManager(ServerSocket server, SQL sqlDB, Socket clientSocket, UUID clientUUID, EventManager eventManager){
		System.out.println(clientUUID);
		this.eventManager = eventManager;
		this.server = server;
		this.clientSocket = clientSocket;
		this.sqlDB = sqlDB;
		this.clientUUID = clientUUID;
	}

	@Override
	public void run(){
		while (!clientSocket.isClosed() && clientSocket.isConnected()){
			try{
				System.out.println("listening");
				DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
				DataInputStream input = new DataInputStream(clientSocket.getInputStream());
				if (!clientSocket.isConnected() || clientSocket.isClosed() || clientSocket.isInputShutdown() || clientSocket.isOutputShutdown()){
					System.out.println("broke");
					break;
				}
				int requestType = input.readInt();
				System.out.println(requestType);
				// -1 = UNSUBSCRIBE | 0 = SEND MESSAGE | 1 = GET LAST x MESSAGES
				if (requestType == 3){
					Main.server.eventManager.unSubscribe(clientSocket);
					Server.connections.remove(clientUUID);
					clientSocket.close();
					break;
				} else if (requestType == 0){
					String message = input.readUTF();
					if (message.length() > 2048){
						System.out.println("Message too long");

					}

					System.out.println("received: " + message);
					BadWordFilter badWordFilter = new BadWordFilter();
					ArrayList<String> badWords = badWordFilter.filter(message);
					System.out.println(message);
					if (!badWords.isEmpty()){
						if (badWords.size() >= message.split(" ").length / 5 - 1){
							System.out.println("inappropriate word detected: " + Arrays.toString(badWords.toArray()));
							return;
						}
					}
					// Add message to the database
					PreparedStatement insertMessagePS = sqlDB.prepareStatement("INSERT INTO messages VALUES(?, ?, ?, ?);");
					insertMessagePS.setString(1, UUID.randomUUID().toString());
					insertMessagePS.setString(2, message);
					insertMessagePS.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
					insertMessagePS.setString(4, clientUUID.toString());
					insertMessagePS.executeUpdate();

					// Get the sender data
					PreparedStatement userDataPS = sqlDB.prepareStatement("SELECT * FROM users WHERE uuid = ?");
					userDataPS.setString(1, clientUUID.toString());
					ResultSet userData = userDataPS.executeQuery();
					userData.next();

					// Send the message
					eventManager.publish(requestType, message, userData.getString("username"), System.currentTimeMillis());
				} else if (requestType == 1){
					int numbOfMessages = input.readInt();
					if (numbOfMessages >= 100){
						output.writeInt(MessageExitCodes.TO_MANY_MESSAGES);
					}
					PreparedStatement getCountPS = sqlDB.prepareStatement("SELECT COUNT(*) FROM messages ORDER BY message_date DESC LIMIT ?;");
					PreparedStatement getMessagesPS = sqlDB.prepareStatement("SELECT * FROM messages LEFT JOIN users ON messages.author_uuid = users.uuid ORDER BY message_date DESC LIMIT ?;");
					getMessagesPS.setInt(1, numbOfMessages);
					getCountPS.setInt(1, numbOfMessages);
					ResultSet countOfRows = getCountPS.executeQuery();
					countOfRows.next();
					int rows = countOfRows.getInt(1);
					if (rows > numbOfMessages){
						rows = numbOfMessages;
					}
					ResultSet messagesRows = getMessagesPS.executeQuery();

					// Tells the client how many we are going to send
					System.out.println("REQTYPE: " + requestType);
					sendTo(clientSocket, requestType, rows);
					System.out.println("ROWS: " + rows);
					messagesRows.next();

					// starts sending rows
					for (int i = 1; i <= rows; i++) {
						System.out.println("CONT: "+messagesRows.getString("message_content"));
						sendTo(clientSocket, -1, messagesRows.getString("message_content"), messagesRows.getString("username"), messagesRows.getTimestamp("message_date").getTime());
						messagesRows.next();
					}

				}
			} catch (Exception e){
				e.printStackTrace();
				try {
					clientSocket.close();
				} catch (IOException ex) {
					e.printStackTrace();
				}
				Server.connections.remove(clientUUID);
				Main.server.eventManager.unSubscribe(clientSocket);
				return;
			}
		}
	}
	public static void sendTo(Socket client, int requestType, Object... dataList)  {
		try {
			DataOutputStream output = new DataOutputStream(client.getOutputStream());

			// Check if requestType is needed
			if (requestType != -1)
				output.writeInt(requestType);

			for (Object data : dataList){
				if (data instanceof String || data instanceof UUID){
					output.writeUTF(data.toString());
				} else if (data instanceof Integer) {
					output.writeInt(((Integer) data));
				} else if (data instanceof Long) {
					output.writeLong(((Long) data));
				} else if (data instanceof Boolean) {
					output.writeBoolean(((Boolean) data));
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
