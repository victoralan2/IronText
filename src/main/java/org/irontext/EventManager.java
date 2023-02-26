package org.irontext;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

public class EventManager {
	private ArrayList<Socket> subscribers = new ArrayList<Socket>();
	public EventManager(){
		new Thread(()->{
			while (true){
				for (Socket socket : subscribers){
					if ( socket.isClosed()) subscribers.remove(socket);
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}).start();
	}


	public void subscribe(Socket subscriber){
		if (subscriber.isClosed()) {
			subscribers.remove(subscriber);
			return;
		}
		subscribers.add(subscriber);
	}

	// BROADCASTS TO EVERY SUBSCRIBER THE DATA
	public void publish(int requestType, Object... dataList)  {
		for (Socket subscriber : subscribers){
			try {
				DataOutputStream output = new DataOutputStream(subscriber.getOutputStream());
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

}
