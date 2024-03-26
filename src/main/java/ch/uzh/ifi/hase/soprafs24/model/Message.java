package ch.uzh.ifi.hase.soprafs24.model;

import lombok.AllArgsConstructor;

import lombok.NoArgsConstructor;

import lombok.ToString;

import ch.uzh.ifi.hase.soprafs24.constant.MessageStatus;

@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Message {
	private String senderName;
	private String receiverName;
	private String message;
	private String date;
	private MessageStatus MessageStatus;


	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}


	public String getReceiverName() {
		return receiverName;
	}

	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public MessageStatus getMessageStatus() {
		return MessageStatus;
	}

	public void setStatus(MessageStatus MessageStatus) {
		this.MessageStatus = MessageStatus;
	}

}