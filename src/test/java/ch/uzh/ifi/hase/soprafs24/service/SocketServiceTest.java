package ch.uzh.ifi.hase.soprafs24.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.constant.RoomProperty;
import ch.uzh.ifi.hase.soprafs24.constant.RoundStatus;
import ch.uzh.ifi.hase.soprafs24.constant.Theme;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SocketServiceTest {

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private ObjectMapper objectMapper;


    private SocketService socketService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        socketService = new SocketService(simpMessagingTemplate, userRepository, playerRepository, roomRepository,
                gameRepository, null, objectMapper); // Ensure objectMapper is injected here
    }

    @Test
    public void broadcastGameinfo_SendsCorrectGameInfo() {
        String roomId = "roomId1";
        String receiptId = "receipt1";
        Room room = mock(Room.class);
        User roomOwner = new User();
        roomOwner.setId("ownerId");
        roomOwner.setUsername("ownerUsername");
        roomOwner.setAvatar("ownerAvatar.jpg");

        Game game = new Game();
        game.setCurrentAnswer("answer");
        game.setRoundStatus(RoundStatus.guess);
        game.setRoundDue("2024-01-01T00:00:00Z");
        game.setCurrentRoundNum(1);
        game.setGameStatus(GameStatus.ingame);

        when(roomRepository.findByRoomId(roomId)).thenReturn(Optional.of(room));
        when(userRepository.findById("ownerId")).thenReturn(Optional.of(roomOwner));
        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.of(game));
        when(room.getRoomOwnerId()).thenReturn("ownerId");
        when(room.getRoomProperty()).thenReturn(RoomProperty.INGAME);

        socketService.broadcastGameinfo(roomId, receiptId);

        verify(simpMessagingTemplate).convertAndSend(eq("/games/info/" + roomId), any(Object.class));
    }

    @Test
    void broadcastPlayerInfo_WaitingRoom_CorrectInfo() {
        String roomId = "roomId1";
        String receiptId = "receipt1";
        Room room = mock(Room.class);
        User user = new User();
        user.setId("userId1");
        user.setUsername("UserOne");
        user.setAvatar("avatar.jpg");

        when(roomRepository.findByRoomId(roomId)).thenReturn(Optional.of(room));
        when(room.getRoomPlayersList()).thenReturn(Arrays.asList("userId1"));
        when(userRepository.findById("userId1")).thenReturn(Optional.of(user));
        when(room.getRoomProperty()).thenReturn(RoomProperty.WAITING);

        socketService.broadcastPlayerInfo(roomId, receiptId);

        // Check if sendMessage is called correctly
        verify(simpMessagingTemplate).convertAndSend(eq("/plays/info/"+roomId), any(Object.class));
    }

    @Test
    void broadcastPlayerInfo_ActiveGame_CorrectInfo() {
        String roomId = "roomId1";
        String receiptId = "receipt1";
        Room room = mock(Room.class);
        User user = new User();
        user.setId("userId1");
        user.setUsername("UserOne");
        user.setAvatar("avatar.jpg");
        Player player = mock(Player.class);

        when(roomRepository.findByRoomId(roomId)).thenReturn(Optional.of(room));
        when(room.getRoomPlayersList()).thenReturn(Arrays.asList("userId1"));
        when(userRepository.findById("userId1")).thenReturn(Optional.of(user));
        when(playerRepository.findById("userId1")).thenReturn(Optional.of(player));
        when(room.getRoomProperty()).thenReturn(RoomProperty.INGAME);
        when(player.getTotalScore()).thenReturn(100);
        when(player.getGuessScore()).thenReturn(50);
        when(player.getSpeakScore()).thenReturn(50);
        when(player.getScoreDetails()).thenReturn(new ArrayList<Map<String, Object>>());
        when(player.getIfGuessed()).thenReturn(true);
        when(player.isRoundFinished()).thenReturn(true);

        socketService.broadcastPlayerInfo(roomId, receiptId);

        // Check if sendMessage is called correctly
        verify(simpMessagingTemplate).convertAndSend(eq("/plays/info/"+roomId), any(Object.class));
    }
    
    @Test
    void broadcastSpeakerAudio_WithNullVoice_CorrectlySendsMessage() {
        String roomId = "room1";
        String userId = "user1";
        String voice = null;

        socketService.broadcastSpeakerAudio(roomId, userId, voice);

        // Verify the sendMessage method is called correctly
        HashMap<String, Object> expectedInfo = new HashMap<>();
        expectedInfo.put("userID", userId);
        expectedInfo.put("audioData", null);
    }

    @Test
    void broadcastLobbyInfo_SendsCorrectLobbyInfo() {
        List<Room> rooms = new ArrayList<>();
        Room room = new Room();
        room.setRoomId("room1");
        room.setRoomName("Room 1");
        room.setRoomOwnerId("owner1");
        room.setTheme(Theme.FOOD);
        room.setMaxPlayersNum(5);
        room.setRoomProperty(RoomProperty.INGAME);
        room.setRoomPlayersList(Arrays.asList("user1"));
        rooms.add(room);

        User user = new User();
        user.setId("user1");
        user.setUsername("User One");
        user.setAvatar("avatar.jpg");

        when(roomRepository.findAll()).thenReturn(rooms);
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));

        socketService.broadcastLobbyInfo();

        verify(roomRepository, times(1)).findAll();
        verify(userRepository, times(2)).findById("user1");
        verify(simpMessagingTemplate).convertAndSend(eq("/lobby/info"), any(Object.class));
    }



}
