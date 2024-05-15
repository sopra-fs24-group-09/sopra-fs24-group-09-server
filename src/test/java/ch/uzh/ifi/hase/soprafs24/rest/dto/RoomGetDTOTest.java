package ch.uzh.ifi.hase.soprafs24.rest.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.uzh.ifi.hase.soprafs24.constant.RoomProperty;
import ch.uzh.ifi.hase.soprafs24.constant.Theme;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoomGetDTOTest {

    @Test
    public void testGettersAndSetters() {
        RoomGetDTO roomGetDTO = new RoomGetDTO();

        String roomName = "Test Room";
        String roomId = "room123";
        Theme theme = Theme.FOOD;
        RoomProperty roomProperty = RoomProperty.WAITING;
        int maxPlayersNum = 10;

        User roomOwner = new User();
        roomOwner.setUsername("owner123");

        List<String> roomPlayersList = Arrays.asList("player1", "player2");
        String roomOwnerId = "owner123";
        ArrayList<User> roomPlayers = new ArrayList<>();
        User player1 = new User();
        player1.setUsername("player1");
        roomPlayers.add(player1);
        User player2 = new User();
        player2.setUsername("player2");
        roomPlayers.add(player2);

        // Set values
        roomGetDTO.setRoomName(roomName);
        roomGetDTO.setRoomId(roomId);
        roomGetDTO.setTheme(theme);
        roomGetDTO.setRoomProperty(roomProperty);
        roomGetDTO.setMaxPlayersNum(maxPlayersNum);
        roomGetDTO.setRoomOwner(roomOwner);
        roomGetDTO.setRoomPlayersList(roomPlayersList);
        roomGetDTO.setRoomOwnerId(roomOwnerId);
        roomGetDTO.setRoomPlayers(roomPlayers);

        // Test getters
        assertEquals(roomName, roomGetDTO.getRoomName());
        assertEquals(roomId, roomGetDTO.getRoomId());
        assertEquals(theme, roomGetDTO.getTheme());
        assertEquals(roomProperty, roomGetDTO.getRoomProperty());
        assertEquals(maxPlayersNum, roomGetDTO.getMaxPlayersNum());
        assertEquals(roomOwner, roomGetDTO.getRoomOwner());
        assertEquals(roomPlayersList, roomGetDTO.getRoomPlayersList());
        assertEquals(roomOwnerId, roomGetDTO.getRoomOwnerId());
        assertEquals(roomPlayers, roomGetDTO.getRoomPlayers());
    }
}
