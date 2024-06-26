package org.irontext;

import java.io.DataOutputStream;
import java.io.IOException;
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
		for (Socket subscriber2 : subscribers){
			if (subscriber2.getInetAddress() == subscriber.getInetAddress()){
				subscribers.remove(subscriber2);
			}
		}
		if (subscriber.isClosed()) {
			subscribers.remove(subscriber);
			return;
		}
		subscribers.add(subscriber);
	}
	public void unSubscribe(Socket subscriber){
		subscribers.remove(subscriber);
	}
	// BROADCASTS TO EVERY SUBSCRIBER THE DATA
	public void publish(int requestType, Object... dataList)  {
		for (Socket subscriber : subscribers){
			try {
				DataOutputStream output = new DataOutputStream(subscriber.getOutputStream());
				output.writeInt(requestType);
				System.out.println("REQTYPE:" + requestType);
				for (Object data : dataList){
					if (data instanceof String || data instanceof UUID){
						System.out.println("DATA: " + data.toString());
						output.writeUTF(data.toString());
					} else if (data instanceof Integer) {
						output.writeInt(((Integer) data));
					} else if (data instanceof Long) {
						output.writeLong(((Long) data));
					} else if (data instanceof Boolean) {
						output.writeBoolean(((Boolean) data));
					}
				}
			} catch (IOException e){
				new Thread(()->unSubscribe(subscriber)).start();
				e.printStackTrace();
			}
		}
	}

}
