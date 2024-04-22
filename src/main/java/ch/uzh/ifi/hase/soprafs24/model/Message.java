package ch.uzh.ifi.hase.soprafs24.model;

import lombok.AllArgsConstructor;

import lombok.NoArgsConstructor;

import lombok.ToString;

import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@ToString

public class Message {
    private String senderName;
    private String receiverName;
    private LocalDateTime timestamp;
    // private MessageOrderType messageType; 
    private Object message;

    // Getters and setters
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

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}