package ch.uzh.ifi.hase.soprafs24.rest.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


public class PlayerGetDTOTest {

    @Test
    public void testGettersAndSetters() {
        PlayerGetDTO playerGetDTO = new PlayerGetDTO();

        String userID = "user123";
        String username = "testUser";
        String avatar = "avatar.png";

        // Set values
        playerGetDTO.setUserID(userID);
        playerGetDTO.setUsername(username);
        playerGetDTO.setAvatar(avatar);

        // Test getters
        assertEquals(userID, playerGetDTO.getUserID());
        assertEquals(username, playerGetDTO.getUsername());
        assertEquals(avatar, playerGetDTO.getAvatar());
    }
}
