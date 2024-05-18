package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.constant.RoomProperty;
import ch.uzh.ifi.hase.soprafs24.constant.Theme;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

public class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private GameService gameService;
    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private RoomService roomService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        roomService = new RoomService(roomRepository, userRepository, gameRepository, playerRepository);
    }

    @Test
    void createRoom_Successful_CreatesRoom() {
        Room newRoom = new Room();
        newRoom.setRoomId("roomId");
        newRoom.setRoomOwnerId("ownerId");
        newRoom.setRoomName("roomName");
        newRoom.setMaxPlayersNum(3);
        newRoom.setTheme(Theme.FOOD);

        when(roomRepository.findByRoomId("roomId")).thenReturn(Optional.empty());
        when(userRepository.findById("ownerId")).thenReturn(Optional.of(new User()));
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArguments()[0]);
        Room createdRoom = roomService.createRoom(newRoom);

        assertNotNull(createdRoom);
        verify(roomRepository).save(newRoom);
        verify(userRepository).save(any(User.class)); // Assuming that the owner's status is set to READY
    }

    @Test
    void createRoom_RoomExists_ThrowsException() {
        Room existingRoom = new Room();
        existingRoom.setRoomId("roomId");

        when(roomRepository.findByRoomId("roomId")).thenReturn(Optional.of(existingRoom));

        assertThrows(ResponseStatusException.class, () -> roomService.createRoom(existingRoom));
    }

    @Test
    void createRoom_Unvalidname(){
        Room newRoom = new Room();
        newRoom.setRoomId("roomId");
        newRoom.setRoomOwnerId("ownerId");
        newRoom.setRoomName("room Name");
        newRoom.setMaxPlayersNum(3);

        when(roomRepository.findByRoomId("roomId")).thenReturn(Optional.empty());
        when(userRepository.findById("ownerId")).thenReturn(Optional.of(new User()));
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArguments()[0]);

        assertThrows(ResponseStatusException.class, () -> roomService.createRoom(newRoom));
    }

    @Test
    void createRoom_UnvalidnumberPlayer() {
        Room newRoom = new Room();
        newRoom.setRoomId("roomId");
        newRoom.setRoomOwnerId("ownerId");
        newRoom.setRoomName("room Name");
        newRoom.setMaxPlayersNum(100);

        when(roomRepository.findByRoomId("roomId")).thenReturn(Optional.empty());
        when(userRepository.findById("ownerId")).thenReturn(Optional.of(new User()));
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArguments()[0]);
        assertThrows(ResponseStatusException.class, () -> roomService.createRoom(newRoom));
    }

    @Test
    void createRoom_Emptyname(){
        Room newRoom = new Room();
        newRoom.setRoomId("roomId");
        newRoom.setRoomOwnerId("ownerId");
        newRoom.setRoomName("");
        newRoom.setMaxPlayersNum(3);

        when(roomRepository.findByRoomId("roomId")).thenReturn(Optional.empty());
        when(userRepository.findById("ownerId")).thenReturn(Optional.of(new User()));
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArguments()[0]);

        assertThrows(ResponseStatusException.class, () -> roomService.createRoom(newRoom));
    }

    @Test
    void createRoom_Special_charactor(){
        Room newRoom = new Room();
        newRoom.setRoomId("roomId");
        newRoom.setRoomOwnerId("ownerId");
        newRoom.setRoomName("%^&^****");
        newRoom.setMaxPlayersNum(3);

        when(roomRepository.findByRoomId("roomId")).thenReturn(Optional.empty());
        when(userRepository.findById("ownerId")).thenReturn(Optional.of(new User()));
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArguments()[0]);

        assertThrows(ResponseStatusException.class, () -> roomService.createRoom(newRoom));
    }


    @Test
    void createRoom_UnexpectedException_ThrowsException() {
        Room newRoom = new Room();
        newRoom.setRoomId("roomId");
        newRoom.setRoomOwnerId("ownerId");
        newRoom.setRoomName("roomName");
        newRoom.setMaxPlayersNum(3);
        newRoom.setTheme(Theme.FOOD);

        when(roomRepository.findByRoomId("roomId")).thenReturn(Optional.empty());
        when(userRepository.findById("ownerId")).thenReturn(Optional.of(new User()));
        when(roomRepository.save(any(Room.class))).thenThrow(new RuntimeException("Unexpected exception"));

        assertThrows(ResponseStatusException.class, () -> roomService.createRoom(newRoom));
    }

    @Test
    void createRoom_EmptyTheme_ThrowsException() {
        Room newRoom = new Room();
        newRoom.setRoomId("roomId");
        newRoom.setRoomOwnerId("ownerId");
        newRoom.setRoomName("roomName");
        newRoom.setMaxPlayersNum(3);
        newRoom.setTheme(null);

        when(roomRepository.findByRoomId("roomId")).thenReturn(Optional.empty());
        when(userRepository.findById("ownerId")).thenReturn(Optional.of(new User()));
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArguments()[0]);

        assertThrows(ResponseStatusException.class, () -> roomService.createRoom(newRoom));
    }

    @Test
    void enterRoom_RoomIsFull_ThrowsException() {
        Room room = new Room();
        room.setMaxPlayersNum(1);
        room.setRoomPlayersList(new ArrayList<>(Collections.singletonList("userId")));

        User user = new User();
        user.setId("newUserId");

        assertThrows(RuntimeException.class, () -> roomService.enterRoom(room, user),
                "Should throw if the room is full.");
    }

    @Test
    void enterRoom_Success_EntersRoom() {
        Room room = new Room();
        room.setMaxPlayersNum(2);
        room.setRoomProperty(RoomProperty.WAITING);
        room.setRoomPlayersList(new ArrayList<>());

        User user = new User();
        user.setId("userId");

        roomService.enterRoom(room, user);

        assertTrue(room.getRoomPlayersList().contains("userId"));
        verify(roomRepository).save(room);
    }

    @Test
    void enterRoom_UserInAnotherRoom_ThrowsException() {
        Room room = new Room();
        room.setMaxPlayersNum(2);
        room.setRoomProperty(RoomProperty.WAITING);
        room.setRoomPlayersList(new ArrayList<>());

        User user = new User();
        user.setId("userId");
        user.setInRoomId("anotherRoomId");

        when(roomRepository.findByRoomId("anotherRoomId")).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> roomService.enterRoom(room, user));
    }

    @Test
    void enterRoom_RoomIsInGame_ThrowsException() {
        Room room = new Room();
        room.setMaxPlayersNum(2);
        room.setRoomProperty(RoomProperty.INGAME);
        room.setRoomPlayersList(new ArrayList<>());

        User user = new User();
        user.setId("userId");

        assertThrows(RuntimeException.class, () -> roomService.enterRoom(room, user),
                "Should throw if the room is in game.");
    }

    @Test
    void exitRoom_UserNotInRoom_ThrowsException() {
        Room room = new Room();
        room.setRoomPlayersList(new ArrayList<>());

        User user = new User();
        user.setId("userId");

        assertThrows(RuntimeException.class, () -> roomService.exitRoom(room, user),
                "Should throw if the user is not in the room.");
    }
    

    @Test
    void exitRoom_UserIsOwnerAndAlone_DeletesRoom() {
        Room room = new Room();
        room.setRoomPlayersList(new ArrayList<>(Collections.singletonList("userId")));
        room.setRoomOwnerId("userId");
        room.setRoomId("roomId");

        User user = new User();
        user.setId("userId");

        when(gameRepository.findByRoomId("roomId")).thenReturn(Optional.empty());

        roomService.exitRoom(room, user);

        verify(roomRepository).delete(room);
    }

    @Test
    void exitRoom_UserIsOwner_DeletesRoom() {
        Room room = new Room();
        Game Game = new Game();
        room.setRoomPlayersList(new ArrayList<>(Collections.singletonList("userId")));
        room.setRoomOwnerId("userId");
        room.setRoomId("roomId");

        User user = new User();
        user.setId("userId");

        when(gameRepository.findByRoomId("roomId")).thenReturn(Optional.of(Game));

        roomService.exitRoom(room, user);

        verify(roomRepository).delete(room);
    }

    @Test
    void exitRoom_UserIsOwner_NotAlone_AssignsNewOwner() {
        Room room = new Room();
        room.setRoomPlayersList(new ArrayList<>(Arrays.asList("userId", "newOwner")));
        room.setRoomOwnerId("userId");

        User user = new User();
        user.setId("userId");
        User newOwner = new User();
        newOwner.setId("newOwner");

        when(userRepository.findById("newOwner")).thenReturn(Optional.of(newOwner));

        roomService.exitRoom(room, user);

        assertEquals("newOwner", room.getRoomOwnerId());
        verify(roomRepository).save(room);
    }

    @Test
    void getWords_ValidTheme_ReturnsListOfWords() {
        String theme = "FOOD";
        try {
            List<String> words = roomService.getWords(theme);
            assertNotNull(words);
            assertFalse(words.isEmpty());
        } catch (IOException e) {
            fail("IOException occurred: " + e.getMessage());
        }
    }

    

}
