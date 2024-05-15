package ch.uzh.ifi.hase.soprafs24.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class MessageTest {

    @Test
    public void testGettersAndSetters() {
        Message message = new Message();

        String senderName = "Alice";
        String receiverName = "Bob";
        LocalDateTime timestamp = LocalDateTime.now();
        Object messageContent = "Hello, this is a test message!";

        // Set values
        message.setSenderName(senderName);
        message.setReceiverName(receiverName);
        message.setTimestamp(timestamp);
        message.setMessage(messageContent);

        // Test getters
        assertEquals(senderName, message.getSenderName());
        assertEquals(receiverName, message.getReceiverName());
        assertEquals(timestamp, message.getTimestamp());
        assertEquals(messageContent, message.getMessage());
    }

    @Test
    public void testAllArgsConstructor() {
        String senderName = "Alice";
        String receiverName = "Bob";
        LocalDateTime timestamp = LocalDateTime.now();
        Object messageContent = "Hello, this is a test message!";

        // Use all args constructor
        Message message = new Message(senderName, receiverName, timestamp, messageContent);

        // Test getters
        assertEquals(senderName, message.getSenderName());
        assertEquals(receiverName, message.getReceiverName());
        assertEquals(timestamp, message.getTimestamp());
        assertEquals(messageContent, message.getMessage());
    }

    @Test
    public void testToString() {
        String senderName = "Alice";
        String receiverName = "Bob";
        LocalDateTime timestamp = LocalDateTime.now();
        Object messageContent = "Hello, this is a test message!";

        // Use all args constructor
        Message message = new Message(senderName, receiverName, timestamp, messageContent);

        String expectedToString = "Message(senderName=Alice, receiverName=Bob, timestamp=" + timestamp + ", message=Hello, this is a test message!)";
        assertEquals(expectedToString, message.toString());
    }
}

