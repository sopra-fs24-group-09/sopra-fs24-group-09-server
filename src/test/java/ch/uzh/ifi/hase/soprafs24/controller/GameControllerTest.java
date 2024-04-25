package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.AnswerGuess;
import ch.uzh.ifi.hase.soprafs24.model.PlayerAndRoom;
import ch.uzh.ifi.hase.soprafs24.model.PlayerAudio;
import ch.uzh.ifi.hase.soprafs24.model.TimestampedRequest;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.PlayerService;
import ch.uzh.ifi.hase.soprafs24.service.RoomService;
import ch.uzh.ifi.hase.soprafs24.service.SocketService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import java.util.HashMap;
import java.util.Optional;

import static org.mockito.Mockito.*;

class GameControllerTest {

    @Mock
    private SocketService socketService;

    @Mock
    private GameService gameService;

    @Mock
    private RoomService roomService;

    @Mock
    private UserService userService;

    @Mock
    private PlayerService playerService;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private GameController gameController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testReady() {
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAndRoom> payload = mock(TimestampedRequest.class);
        PlayerAndRoom playerAndRoom = mock(PlayerAndRoom.class); // Mock the PlayerAndRoom object
        String receiptId = "receiptId";
        String userId = "userID";
        String roomId = "roomID";
    
        when(headerAccessor.getHeader("receipt")).thenReturn(receiptId);
        when(payload.getMessage()).thenReturn(playerAndRoom); // Ensure getMessage() does not return null
        when(playerAndRoom.getUserID()).thenReturn(userId); // Set up getPlayerID() to return a specific user ID
        when(playerAndRoom.getRoomID()).thenReturn(roomId); // Set up getRoomID() to return a specific room ID
    
        gameController.ready(headerAccessor, payload);
    
        verify(gameService).Ready(userId);
        verify(socketService).broadcastPlayerInfo(roomId, receiptId);
        verify(socketService).broadcastGameinfo(roomId, receiptId);
    }

    @Test
    void testUnready() {
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAndRoom> payload = mock(TimestampedRequest.class);
        PlayerAndRoom playerAndRoom = mock(PlayerAndRoom.class); // Mock the PlayerAndRoom object
        String receiptId = "receiptId";
        String userId = "userID";
        String roomId = "roomID";
    
        when(headerAccessor.getHeader("receipt")).thenReturn(receiptId);
        when(payload.getMessage()).thenReturn(playerAndRoom); // Ensure getMessage() does not return null
        when(playerAndRoom.getUserID()).thenReturn(userId); // Set up getPlayerID() to return a specific user ID
        when(playerAndRoom.getRoomID()).thenReturn(roomId); // Set up getRoomID() to return a specific room ID
    
        gameController.unready(headerAccessor, payload);
    
        verify(gameService).UnReady(userId);
        verify(socketService).broadcastPlayerInfo(roomId, receiptId);
        verify(socketService).broadcastGameinfo(roomId, receiptId);
    }

    @Test
    void testEnterRoom() {
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAndRoom> payload = mock(TimestampedRequest.class);
        PlayerAndRoom playerAndRoom = mock(PlayerAndRoom.class); // Mock the PlayerAndRoom object
        String receiptId = "receiptId";
        String roomId = "roomID";
        String userId = "userID";
        Room room = mock(Room.class);
        User user = mock(User.class);
    
        when(headerAccessor.getHeader("receipt")).thenReturn(receiptId);
        when(headerAccessor.getSessionAttributes()).thenReturn(new HashMap<String, Object>() {{
            put("roomId", roomId);
        }});
        when(payload.getMessage()).thenReturn(playerAndRoom); // Ensure getMessage() does not return null
        when(playerAndRoom.getUserID()).thenReturn(userId); // Set up getPlayerID() to return a specific user ID
        when(roomService.findRoomById(roomId)).thenReturn(room);
        when(userService.findUserById(userId)).thenReturn(user);
    
        gameController.enterRoom(headerAccessor, payload);
    
        verify(roomService).enterRoom(room, user);
        verify(socketService).broadcastGameinfo(roomId, receiptId);
        verify(socketService).broadcastPlayerInfo(roomId, "enterroom");
    }

    @Test
    void testExitRoom() {
        // Set up headerAccessor and payload
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAndRoom> payload = mock(TimestampedRequest.class);
        PlayerAndRoom playerAndRoom = mock(PlayerAndRoom.class);
        Room room = mock(Room.class);
        User user = mock(User.class);
        String receiptId = "receiptId";
        String roomId = "roomId";
        String userId = "userId";

        when(headerAccessor.getHeader("receipt")).thenReturn(receiptId);
        when(headerAccessor.getSessionAttributes()).thenReturn(new HashMap<String, Object>() {{ put("roomId", roomId); }});
        when(payload.getMessage()).thenReturn(playerAndRoom);
        when(playerAndRoom.getUserID()).thenReturn(userId);
        when(roomService.findRoomById(roomId)).thenReturn(room);
        when(userService.findUserById(userId)).thenReturn(user);

        // Additional setup for gameRepository
        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.of(new Game()));

        // Execute the test method
        gameController.exitRoom(headerAccessor, payload);
        
        // Verify broadcast methods are called since game exists for the room
        verify(socketService).broadcastGameinfo(roomId, receiptId);
        verify(socketService).broadcastPlayerInfo(roomId, "exitroom");
    }


    @Test
    void testStartGame() {
        // Mock the header accessor and payload
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAndRoom> payload = mock(TimestampedRequest.class);
        PlayerAndRoom playerAndRoom = mock(PlayerAndRoom.class);

        // Test data
        String receiptId = "receiptId";
        String roomId = "roomID";
        Room room = mock(Room.class);

        // Setting up the mocks
        when(headerAccessor.getHeader("receipt")).thenReturn(receiptId);
        when(payload.getMessage()).thenReturn(playerAndRoom);
        when(playerAndRoom.getRoomID()).thenReturn(roomId);
        when(roomService.findRoomById(roomId)).thenReturn(room);

        // Calling the method under test
        gameController.startGame(headerAccessor, payload);

        // Verifying that the correct services were called
        verify(roomService).findRoomById(roomId);
        verify(gameService).checkIfAllReady(room);
        
        // Verifying no more interactions
        verifyNoMoreInteractions(roomService);
        verifyNoMoreInteractions(gameService);
    }

    @Test
    void testSubmitAnswer() {
        // Mocking the header accessor and payload
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<AnswerGuess> payload = mock(TimestampedRequest.class);
        AnswerGuess answerGuess = mock(AnswerGuess.class);

        // Test data
        String receiptId = "receiptId";
        String userId = "userID";
        String roomId = "roomID";
        String guess = "guess";
        Game game = mock(Game.class);
        Player player = mock(Player.class);

        // Setting up mocks
        when(headerAccessor.getHeader("receipt")).thenReturn(receiptId);
        when(payload.getMessage()).thenReturn(answerGuess);
        when(answerGuess.getUserID()).thenReturn(userId);
        when(answerGuess.getRoomID()).thenReturn(roomId);
        when(answerGuess.getGuess()).thenReturn(guess);
        when(gameService.findGameById(roomId)).thenReturn(game);
        when(playerService.findPlayerById(userId)).thenReturn(player);

        // Method under test
        gameController.submitAnswer(headerAccessor, payload);

        // Verifications
        verify(gameService).findGameById(roomId);
        verify(playerService).findPlayerById(userId);
        verify(gameService).validateAnswer(game, player, guess);
        
        // Additional checks
        verifyNoMoreInteractions(gameService, playerService);
    }

    @Test
    void testUploadAudio() {
        // Mocking header accessor and payload
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAudio> payload = mock(TimestampedRequest.class);
        PlayerAudio playerAudio = mock(PlayerAudio.class);

        // Define test data
        String receiptId = "receiptId";
        String userId = "userID";
        String roomId = "roomID";
        String audioData = "audioData";

        // Set up mock behaviors
        when(headerAccessor.getHeader("receipt")).thenReturn(receiptId);
        when(headerAccessor.getSessionAttributes()).thenReturn(new HashMap<String, Object>() {{
            put("roomId", roomId);
        }});
        when(payload.getMessage()).thenReturn(playerAudio);
        when(playerAudio.getUserID()).thenReturn(userId);
        when(playerAudio.getAudioData()).thenReturn(audioData);

        // Method under test
        gameController.uploadAudio(headerAccessor, payload);

        // Verify the interactions
        verify(headerAccessor).getSessionAttributes();
        verify(gameService).setPlayerAudio(roomId, userId, audioData);

        // Ensure no unnecessary interactions
        verifyNoMoreInteractions(gameService);
    }




}