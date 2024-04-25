package ch.uzh.ifi.hase.soprafs24.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.constant.RoomProperty;
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

    private RoomService roomService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        roomService = new RoomService(roomRepository, userRepository, gameRepository);
    }

    @Test
    void createRoom_Successful_CreatesRoom() {
        Room newRoom = new Room();
        newRoom.setRoomId("roomId");
        newRoom.setRoomOwnerId("ownerId");

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
    void enterRoom_RoomIsFull_ThrowsException() {
        Room room = new Room();
        room.setMaxPlayersNum(1);
        room.setRoomPlayersList(new ArrayList<>(Collections.singletonList("userId")));

        User user = new User();
        user.setId("newUserId");

        assertThrows(ResponseStatusException.class, () -> roomService.enterRoom(room, user),
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
    void enterRoom_FullRoom_ThrowsException() {
        Room room = new Room();
        room.setMaxPlayersNum(1);
        room.setRoomProperty(RoomProperty.WAITING);
        room.setRoomPlayersList(new ArrayList<>(Arrays.asList("userId")));

        User newUser = new User();
        newUser.setId("newUserId");

        assertThrows(ResponseStatusException.class, () -> roomService.enterRoom(room, newUser));
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

}
