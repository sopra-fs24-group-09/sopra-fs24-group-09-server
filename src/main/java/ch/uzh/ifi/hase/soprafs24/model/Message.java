package ch.uzh.ifi.hase.soprafs24.model;

import lombok.AllArgsConstructor;

import lombok.NoArgsConstructor;

import lombok.ToString;

import java.time.LocalDateTime;

import ch.uzh.ifi.hase.soprafs24.constant.MessageType;

@NoArgsConstructor
@AllArgsConstructor
@ToString

public class Message {
    private String senderName;
    private String receiverName;
    private String message;
    private LocalDateTime timestamp; // Changed from String date to LocalDateTime timestamp
    private MessageType messageStatus; // Changed field name to camelCase

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public MessageType getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(MessageType messageStatus) {
        this.messageStatus = messageStatus;
    }
}