package org.example;

public class Message {


	private String content;
	private String sender;
	private long timestamp;

	public Message(String content, String sender, long timestamp){
		this.content = content;
		this.sender = sender;
		this.timestamp = timestamp;
	}


	public String getContent() {
		return content;
	}

	public String getSender() {
		return sender;
	}

	public long getTimestamp() {
		return timestamp;
	}
}
