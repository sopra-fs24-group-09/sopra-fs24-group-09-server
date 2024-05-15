package ch.uzh.ifi.hase.soprafs24.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PlayerAudioTest {

    @Test
    public void testGettersAndSetters() {
        PlayerAudio playerAudio = new PlayerAudio();

        String userID = "user123";
        String roomID = "room123";
        String audioData = "audio data";

        // Set values
        playerAudio.setUserID(userID);
        playerAudio.setRoomID(roomID);
        playerAudio.setAudioData(audioData);

        // Test getters
        assertEquals(userID, playerAudio.getUserID());
        assertEquals(roomID, playerAudio.getRoomID());
        assertEquals(audioData, playerAudio.getAudioData());
    }
}
