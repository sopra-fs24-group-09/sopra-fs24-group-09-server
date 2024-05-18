package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.constant.RoomProperty;
import ch.uzh.ifi.hase.soprafs24.constant.RoundStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.AnswerGuess;
import ch.uzh.ifi.hase.soprafs24.model.PlayerAndRoom;
import ch.uzh.ifi.hase.soprafs24.model.PlayerAudio;
import ch.uzh.ifi.hase.soprafs24.model.TimestampedRequest;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
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

import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

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

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private UserRepository userRepository;

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
        PlayerAndRoom playerAndRoom = mock(PlayerAndRoom.class);
        String receiptId = "receiptId";
        String userId = "userID";
        String roomId = "roomID";
        String token = "validToken";

        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put("receiptId", List.of(receiptId));
        nativeHeaders.put("token", List.of(token));

        when(headerAccessor.getHeader("nativeHeaders")).thenReturn(nativeHeaders);
        when(payload.getMessage()).thenReturn(playerAndRoom);
        when(playerAndRoom.getUserID()).thenReturn(userId);
        when(playerAndRoom.getRoomID()).thenReturn(roomId);
        when(userService.findByToken(token)).thenReturn(true);

        gameController.ready(headerAccessor, roomId, payload);

        verify(gameService).Ready(userId, roomId);
        verify(socketService).broadcastResponse(userId, roomId, true, true, "unready " + userId, receiptId);
        verify(socketService).broadcastPlayerInfo(roomId, receiptId);
        verify(socketService).broadcastGameinfo(roomId, receiptId);
    }

    @Test
    void testReadyInvalidToken() {
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAndRoom> payload = mock(TimestampedRequest.class);
        PlayerAndRoom playerAndRoom = mock(PlayerAndRoom.class);
        String receiptId = "receiptId";
        String userId = "userID";
        String roomId = "roomID";
        String token = "invalidToken";

        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put("receiptId", List.of(receiptId));
        nativeHeaders.put("token", List.of(token));

        when(headerAccessor.getHeader("nativeHeaders")).thenReturn(nativeHeaders);
        when(payload.getMessage()).thenReturn(playerAndRoom);
        when(playerAndRoom.getUserID()).thenReturn(userId);
        when(userService.findByToken(token)).thenReturn(false);

        gameController.ready(headerAccessor, roomId, payload);

        verify(socketService).broadcastResponse(userId, roomId, false, false, "Invalid or expired token", receiptId);
        verify(gameService, never()).Ready(anyString(), anyString());
    }

    @Test
    void testReadyException() {
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAndRoom> payload = mock(TimestampedRequest.class);
        PlayerAndRoom playerAndRoom = mock(PlayerAndRoom.class);
        String receiptId = "receiptId";
        String userId = "userID";
        String roomId = "roomID";
        String token = "validToken";

        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put("receiptId", List.of(receiptId));
        nativeHeaders.put("token", List.of(token));

        when(headerAccessor.getHeader("nativeHeaders")).thenReturn(nativeHeaders);
        when(payload.getMessage()).thenReturn(playerAndRoom);
        when(playerAndRoom.getUserID()).thenReturn(userId);
        when(userService.findByToken(token)).thenReturn(true);
        doThrow(new RuntimeException("Test Exception")).when(gameService).Ready(userId, roomId);

        gameController.ready(headerAccessor, roomId, payload);

        verify(socketService).broadcastResponse(userId, roomId, false, true, "Failed to unready: Test Exception", receiptId);
        verify(socketService, never()).broadcastPlayerInfo(roomId, receiptId);
        verify(socketService, never()).broadcastGameinfo(roomId, receiptId);
    }

    @Test
    void testUnready() {
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAndRoom> payload = mock(TimestampedRequest.class);
        PlayerAndRoom playerAndRoom = mock(PlayerAndRoom.class);
        String receiptId = "receiptId";
        String userId = "userID";
        String roomId = "roomID";
        String token = "validToken";

        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put("receiptId", List.of(receiptId));
        nativeHeaders.put("token", List.of(token));

        when(headerAccessor.getHeader("nativeHeaders")).thenReturn(nativeHeaders);
        when(payload.getMessage()).thenReturn(playerAndRoom);
        when(playerAndRoom.getUserID()).thenReturn(userId);
        when(playerAndRoom.getRoomID()).thenReturn(roomId);
        when(userService.findByToken(token)).thenReturn(true);

        gameController.unready(headerAccessor, roomId, payload);

        verify(gameService).UnReady(userId);
        verify(socketService).broadcastResponse(userId, roomId, true, true, "unready " + userId, receiptId);
        verify(socketService).broadcastPlayerInfo(roomId, receiptId);
        verify(socketService).broadcastGameinfo(roomId, receiptId);
    }

    @Test
    void testEnterRoom() {
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAndRoom> payload = mock(TimestampedRequest.class);
        PlayerAndRoom playerAndRoom = mock(PlayerAndRoom.class);
        String receiptId = "receiptId";
        String roomId = "roomId";
        String userId = "userId";
        String token = "validToken";
        Room room = mock(Room.class);
        User user = mock(User.class);
        Game game = mock(Game.class);

        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put("receiptId", List.of(receiptId));
        nativeHeaders.put("token", List.of(token));

        when(headerAccessor.getHeader("nativeHeaders")).thenReturn(nativeHeaders);
        when(payload.getMessage()).thenReturn(playerAndRoom);
        when(playerAndRoom.getUserID()).thenReturn(userId);
        when(userService.findByToken(token)).thenReturn(true);
        when(roomRepository.findByRoomId(roomId)).thenReturn(Optional.of(room));
        when(userService.findUserById(userId)).thenReturn(user);
        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.of(game));
        when(room.getRoomPlayersList()).thenReturn(Collections.singletonList(userId));
        when(room.getRoomProperty()).thenReturn(RoomProperty.INGAME);
        when(game.getRoundStatus()).thenReturn(RoundStatus.guess);
        when(game.getCurrentSpeaker()).thenReturn(mock(Player.class));

        // Call the controller method
        gameController.enterRoom(headerAccessor, roomId, payload);

        // Verify interactions
        verify(roomService).enterRoom(room, user);
        verify(socketService).broadcastGameinfo(roomId, receiptId);
        verify(socketService).broadcastPlayerInfo(roomId, "enterRoom");
        verify(socketService).broadcastLobbyInfo();
        verify(socketService).broadcastResponse(userId, roomId, true, true, "enter room", receiptId);
    }

    @Test
    void testEnterRoom_gameover() {
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAndRoom> payload = mock(TimestampedRequest.class);
        PlayerAndRoom playerAndRoom = mock(PlayerAndRoom.class);
        String receiptId = "receiptId";
        String roomId = "roomId";
        String userId = "userId";
        String token = "validToken";
        Room room = mock(Room.class);
        User user = mock(User.class);
        Game game = mock(Game.class);

        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put("receiptId", List.of(receiptId));
        nativeHeaders.put("token", List.of(token));

        when(headerAccessor.getHeader("nativeHeaders")).thenReturn(nativeHeaders);
        when(payload.getMessage()).thenReturn(playerAndRoom);
        when(playerAndRoom.getUserID()).thenReturn(userId);
        when(user.getId()).thenReturn(userId); // Ensure user.getId() returns userId
        when(userService.findByToken(token)).thenReturn(true);
        when(roomRepository.findByRoomId(roomId)).thenReturn(Optional.of(room));
        when(userService.findUserById(userId)).thenReturn(user);
        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.of(game));
        when(room.getRoomPlayersList()).thenReturn(Collections.singletonList(userId));
        when(room.getRoomProperty()).thenReturn(RoomProperty.GAMEOVER);
        when(game.getRoundStatus()).thenReturn(RoundStatus.guess);
        when(game.getCurrentSpeaker()).thenReturn(mock(Player.class));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // Call the controller method
        gameController.enterRoom(headerAccessor, roomId, payload);

        // Verify interactions
        verify(socketService).broadcastResponse(userId, roomId, false, true, "Game is over", receiptId);
     
    }

    @Test
    void testEnterRoomAlreadyInRoom() {
        // Mock necessary objects
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAndRoom> payload = mock(TimestampedRequest.class);
        PlayerAndRoom playerAndRoom = mock(PlayerAndRoom.class);
        Player currentSpeaker = mock(Player.class);
        String receiptId = "receiptId";
        String roomId = "roomId";
        String userId = "userId";
        String token = "validToken";
        Room room = mock(Room.class);
        Game game = mock(Game.class);
        User user = mock(User.class);
    
        // Mocking headers
        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put("receiptId", List.of(receiptId));
        nativeHeaders.put("token", List.of(token));
    
        when(headerAccessor.getHeader("nativeHeaders")).thenReturn(nativeHeaders);
        when(payload.getMessage()).thenReturn(playerAndRoom);
        when(playerAndRoom.getUserID()).thenReturn(userId);
        when(userService.findByToken(token)).thenReturn(true);
        when(roomRepository.findByRoomId(roomId)).thenReturn(Optional.of(room));
        when(userService.findUserById(userId)).thenReturn(user);
        when(user.getId()).thenReturn(userId); // Ensure user.getId() returns userId
        when(room.getRoomPlayersList()).thenReturn(Collections.singletonList(userId)); // Ensure room contains userId
        when(room.getRoomProperty()).thenReturn(RoomProperty.INGAME);
        when(room.getRoomId()).thenReturn(roomId);
        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.of(game));

        when(game.getRoundStatus()).thenReturn(RoundStatus.guess);
        when(game.getCurrentSpeaker()).thenReturn(currentSpeaker);
        when(currentSpeaker.getId()).thenReturn("speakerId");
        when(playerRepository.findById("speakerId")).thenReturn(Optional.of(currentSpeaker));
        when(currentSpeaker.getAudioData()).thenReturn("audioData");
    
        // Call the controller method
        gameController.enterRoom(headerAccessor, roomId, payload);
    
        // Verify interactions for when user is already in the room and game is started and in guess round
        verify(socketService,times(2)).broadcastGameinfo(roomId, receiptId);
        verify(socketService,times(2)).broadcastPlayerInfo(roomId, "enterroom");
        verify(socketService, times(2)).broadcastLobbyInfo(); 
        verify(socketService).broadcastSpeakerAudio(null, "speakerId", "audioData");
        verify(socketService).broadcastResponse(userId, roomId, true, true, "enter room", receiptId);
    }

    @Test
    void testEnterRoomInvalidToken() {
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAndRoom> payload = mock(TimestampedRequest.class);
        PlayerAndRoom playerAndRoom = mock(PlayerAndRoom.class);
        String receiptId = "receiptId";
        String roomId = "roomId";
        String userId = "userId";
        String token = "invalidToken";

        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put("receiptId", List.of(receiptId));
        nativeHeaders.put("token", List.of(token));

        when(headerAccessor.getHeader("nativeHeaders")).thenReturn(nativeHeaders);
        when(payload.getMessage()).thenReturn(playerAndRoom);
        when(playerAndRoom.getUserID()).thenReturn(userId);
        when(userService.findByToken(token)).thenReturn(false);

        // Call the controller method
        gameController.enterRoom(headerAccessor, roomId, payload);

        // Verify interactions
        verify(socketService).broadcastResponse(userId, roomId, false, false, "Invalid or expired token", receiptId);
    }

    @Test
    void testEnterRoomNotFound() {
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAndRoom> payload = mock(TimestampedRequest.class);
        PlayerAndRoom playerAndRoom = mock(PlayerAndRoom.class);
        String receiptId = "receiptId";
        String roomId = "roomId";
        String userId = "userId";
        String token = "validToken";
        User user = mock(User.class); // Mock User object

        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put("receiptId", List.of(receiptId));
        nativeHeaders.put("token", List.of(token));

        when(headerAccessor.getHeader("nativeHeaders")).thenReturn(nativeHeaders);
        when(payload.getMessage()).thenReturn(playerAndRoom);
        when(playerAndRoom.getUserID()).thenReturn(userId);
        when(userService.findByToken(token)).thenReturn(true);
        when(roomRepository.findByRoomId(roomId)).thenReturn(Optional.empty());
        when(userService.findUserById(userId)).thenReturn(user); // Return a mocked user

        // Call the controller method
        gameController.enterRoom(headerAccessor, roomId, payload);

        // Verify interactions
        verify(socketService).broadcastResponse(userId, roomId, false, true, "Room not found", receiptId);
    }
    

    @Test
    void testExitRoom() {
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAndRoom> payload = mock(TimestampedRequest.class);
        PlayerAndRoom playerAndRoom = mock(PlayerAndRoom.class);
        String receiptId = "receiptId";
        String roomId = "roomId";
        String userId = "userId";
        String token = "validToken";
        Room room = mock(Room.class);
        User user = mock(User.class);

        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put("receiptId", List.of(receiptId));
        nativeHeaders.put("token", List.of(token));

        when(headerAccessor.getHeader("nativeHeaders")).thenReturn(nativeHeaders);
        when(payload.getMessage()).thenReturn(playerAndRoom);
        when(playerAndRoom.getUserID()).thenReturn(userId);
        when(userService.findByToken(token)).thenReturn(true);

        when(roomRepository.findByRoomId(roomId)).thenReturn(Optional.of(room));
        when(userService.findUserById(userId)).thenReturn(user);
        when(room.getRoomPlayersList()).thenReturn(Collections.singletonList(userId));
        when(user.getId()).thenReturn(userId); // Ensure user.getId() returns userId
        when(room.getRoomProperty()).thenReturn(RoomProperty.WAITING); // Room is not in game

        // Call the controller method
        gameController.exitRoom(headerAccessor, roomId, payload);

        // Verify interactions
        verify(roomService).exitRoom(room, user);
        verify(socketService).broadcastLobbyInfo();
        verify(socketService).broadcastPlayerInfo(roomId, "exitroom");
        verify(socketService).broadcastGameinfo(roomId, receiptId);
        verify(socketService).broadcastResponse(userId, roomId, true, true, "Successfully exited room", receiptId);
    }

    @Test
    void testExitRoomInvalidToken() {
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAndRoom> payload = mock(TimestampedRequest.class);
        PlayerAndRoom playerAndRoom = mock(PlayerAndRoom.class);
        String receiptId = "receiptId";
        String roomId = "roomId";
        String userId = "userId";
        String token = "invalidToken";

        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put("receiptId", List.of(receiptId));
        nativeHeaders.put("token", List.of(token));

        when(headerAccessor.getHeader("nativeHeaders")).thenReturn(nativeHeaders);
        when(payload.getMessage()).thenReturn(playerAndRoom);
        when(playerAndRoom.getUserID()).thenReturn(userId);
        when(userService.findByToken(token)).thenReturn(false);

        // Call the controller method
        gameController.exitRoom(headerAccessor, roomId, payload);

        // Verify interactions
        verify(socketService).broadcastResponse(userId, roomId, false, false, "Invalid or expired token", receiptId);
    }

    @Test
    void testExitRoomGameInProgress() {
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAndRoom> payload = mock(TimestampedRequest.class);
        PlayerAndRoom playerAndRoom = mock(PlayerAndRoom.class);
        String receiptId = "receiptId";
        String roomId = "roomId";
        String userId = "userId";
        String token = "validToken";
        Room room = mock(Room.class);
        User user = mock(User.class);

        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put("receiptId", List.of(receiptId));
        nativeHeaders.put("token", List.of(token));

        when(headerAccessor.getHeader("nativeHeaders")).thenReturn(nativeHeaders);
        when(payload.getMessage()).thenReturn(playerAndRoom);
        when(playerAndRoom.getUserID()).thenReturn(userId);
        when(userService.findByToken(token)).thenReturn(true);
        when(roomRepository.findByRoomId(roomId)).thenReturn(Optional.of(room));
        when(userService.findUserById(userId)).thenReturn(user);
        when(room.getRoomProperty()).thenReturn(RoomProperty.INGAME); // Room is in game

        // Call the controller method
        gameController.exitRoom(headerAccessor, roomId, payload);

        // Verify interactions
        verify(socketService).broadcastResponse(userId, roomId, false, true, "Failed to exit room: Cannot exit room while game is in progress", receiptId);
    }

    @Test
    void testExitRoomNotFound() {
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAndRoom> payload = mock(TimestampedRequest.class);
        PlayerAndRoom playerAndRoom = mock(PlayerAndRoom.class);
        String receiptId = "receiptId";
        String roomId = "roomId";
        String userId = "userId";
        String token = "validToken";
        User user = mock(User.class);

        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put("receiptId", List.of(receiptId));
        nativeHeaders.put("token", List.of(token));

        when(headerAccessor.getHeader("nativeHeaders")).thenReturn(nativeHeaders);
        when(payload.getMessage()).thenReturn(playerAndRoom);
        when(playerAndRoom.getUserID()).thenReturn(userId);
        when(userService.findByToken(token)).thenReturn(true);
        when(roomRepository.findByRoomId(roomId)).thenReturn(Optional.empty());
        when(userService.findUserById(userId)).thenReturn(user);

        // Call the controller method
        gameController.exitRoom(headerAccessor, roomId, payload);

        // Verify interactions
        verifyNoInteractions(socketService);
    }


    @Test
    void testStartGame_withValidTokenAndRoom() {
        String roomID = "room1";
        String userID = "user1";
        String receiptID = "receipt1";
        String token = "valid-token";
        PlayerAndRoom playerAndRoom = mock(PlayerAndRoom.class);
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAndRoom> payload = mock(TimestampedRequest.class);

        // Setting up nativeHeaders
        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put("receiptId", Collections.singletonList(receiptID));
        nativeHeaders.put("token", Collections.singletonList(token));
        when(headerAccessor.getHeader("nativeHeaders")).thenReturn(nativeHeaders);
        when(payload.getMessage()).thenReturn(playerAndRoom);
        when(playerAndRoom.getUserID()).thenReturn(userID);

        Room room = new Room();
        room.setRoomId(roomID);
        List<String> roomWordsList = new ArrayList<>();
        roomWordsList.add("word1");
        room.setRoomWordsList(roomWordsList);

        when(userService.findByToken(token)).thenReturn(true);
        when(roomRepository.findByRoomId(roomID)).thenReturn(Optional.of(room));

        gameController.startGame(headerAccessor, roomID, payload);

        verify(gameService, times(1)).checkIfAllReady(room);
        verify(gameService, times(1)).startGame(room);
        verify(socketService, times(1)).broadcastResponse(userID, roomID, true, true, "Game started successfully", receiptID);
    }



    @Test
    void testSubmitAnswer_withValidToken() {
        String roomID = "room1";
        String userID = "user1";
        String receiptID = "receipt1";
        String token = "valid-token";
        String guess = "validGuess";
        AnswerGuess answerGuess = mock(AnswerGuess.class);
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<AnswerGuess> payload = mock(TimestampedRequest.class);

        // Setting up nativeHeaders
        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put("receiptId", Collections.singletonList(receiptID));
        nativeHeaders.put("token", Collections.singletonList(token));
        when(headerAccessor.getHeader("nativeHeaders")).thenReturn(nativeHeaders);
        when(payload.getMessage()).thenReturn(answerGuess);
        when(answerGuess.getUserID()).thenReturn(userID);
        when(answerGuess.getGuess()).thenReturn(guess);

        Game game = new Game();
        Player player = new Player();

        when(userService.findByToken(token)).thenReturn(true);
        when(gameService.findGameById(roomID)).thenReturn(game);
        when(playerService.findPlayerById(userID)).thenReturn(player);

        gameController.submitAnswer(headerAccessor, roomID, payload);

        verify(gameService, times(1)).validateAnswer(game, player, guess.replaceAll("[^a-zA-Z0-9]", ""));
        verify(socketService, times(1)).broadcastResponse(userID, roomID, true, true, "Answer submitted successfully", receiptID);
    }

    @Test
    void testSubmitAnswer_withInvalidToken() {
        String roomID = "room1";
        String userID = "user1";
        String receiptID = "receipt1";
        String token = "invalid-token";
        String guess = "validGuess";
        AnswerGuess answerGuess = mock(AnswerGuess.class);
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<AnswerGuess> payload = mock(TimestampedRequest.class);

        // Setting up nativeHeaders
        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put("receiptId", Collections.singletonList(receiptID));
        nativeHeaders.put("token", Collections.singletonList(token));
        when(headerAccessor.getHeader("nativeHeaders")).thenReturn(nativeHeaders);
        when(payload.getMessage()).thenReturn(answerGuess);
        when(answerGuess.getUserID()).thenReturn(userID);
        when(answerGuess.getGuess()).thenReturn(guess);

        when(userService.findByToken(token)).thenReturn(false);

        gameController.submitAnswer(headerAccessor, roomID, payload);

        verify(gameService, never()).validateAnswer(any(), any(), any());
        verify(socketService, times(1)).broadcastResponse(userID, roomID, false, false, "Invalid or expired token", receiptID);
    }

    @Test
    void testSubmitAnswer_gameNotFound() {
        String roomID = "room1";
        String userID = "user1";
        String receiptID = "receipt1";
        String token = "valid-token";
        String guess = "validGuess";
        AnswerGuess answerGuess = mock(AnswerGuess.class);
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<AnswerGuess> payload = mock(TimestampedRequest.class);

        // Setting up nativeHeaders
        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put("receiptId", Collections.singletonList(receiptID));
        nativeHeaders.put("token", Collections.singletonList(token));
        when(headerAccessor.getHeader("nativeHeaders")).thenReturn(nativeHeaders);
        when(payload.getMessage()).thenReturn(answerGuess);
        when(answerGuess.getUserID()).thenReturn(userID);
        when(answerGuess.getGuess()).thenReturn(guess);

        when(userService.findByToken(token)).thenReturn(true);
        when(gameService.findGameById(roomID)).thenThrow(new RuntimeException("Game not found"));

        gameController.submitAnswer(headerAccessor, roomID, payload);

        verify(gameService, never()).validateAnswer(any(), any(), any());
        verify(socketService, times(1)).broadcastResponse(userID, roomID, false, true, "Failed to validate answer: Game not found", receiptID);
    }

    @Test
    void testSubmitAnswer_withException() {
        String roomID = "room1";
        String userID = "user1";
        String receiptID = "receipt1";
        String token = "valid-token";
        String guess = "validGuess";
        AnswerGuess answerGuess = mock(AnswerGuess.class);
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<AnswerGuess> payload = mock(TimestampedRequest.class);

        // Setting up nativeHeaders
        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put("receiptId", Collections.singletonList(receiptID));
        nativeHeaders.put("token", Collections.singletonList(token));
        when(headerAccessor.getHeader("nativeHeaders")).thenReturn(nativeHeaders);
        when(payload.getMessage()).thenReturn(answerGuess);
        when(answerGuess.getUserID()).thenReturn(userID);
        when(answerGuess.getGuess()).thenReturn(guess);

        Game game = new Game();
        Player player = new Player();

        when(userService.findByToken(token)).thenReturn(true);
        when(gameService.findGameById(roomID)).thenReturn(game);
        when(playerService.findPlayerById(userID)).thenReturn(player);
        doThrow(new RuntimeException("Test exception")).when(gameService).validateAnswer(game, player, guess.replaceAll("[^a-zA-Z0-9]", ""));

        gameController.submitAnswer(headerAccessor, roomID, payload);

        verify(gameService, times(1)).validateAnswer(game, player, guess.replaceAll("[^a-zA-Z0-9]", ""));
        verify(socketService, times(1)).broadcastResponse(userID, roomID, false, true, "Failed to validate answer: Test exception", receiptID);
    }


    @Test
    void testUploadAudio_withValidToken() {
        String roomID = "room1";
        String userID = "user1";
        String receiptID = "receipt1";
        String token = "valid-token";
        String voice = "audioData";
        PlayerAudio playerAudio = mock(PlayerAudio.class);
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAudio> payload = mock(TimestampedRequest.class);

        // Setting up nativeHeaders
        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put("receiptId", Collections.singletonList(receiptID));
        nativeHeaders.put("token", Collections.singletonList(token));
        when(headerAccessor.getHeader("nativeHeaders")).thenReturn(nativeHeaders);
        when(payload.getMessage()).thenReturn(playerAudio);
        when(playerAudio.getUserID()).thenReturn(userID);
        when(playerAudio.getAudioData()).thenReturn(voice);

        when(userService.findByToken(token)).thenReturn(true);

        gameController.uploadAudio(headerAccessor, roomID, payload);

        verify(gameService, times(1)).setPlayerAudio(roomID, userID, voice);
        verify(socketService, times(1)).broadcastResponse(userID, roomID, true, true, "Audio uploaded successfully", receiptID);
    }

    @Test
    void testUploadAudio_withInvalidToken() {
        String roomID = "room1";
        String userID = "user1";
        String receiptID = "receipt1";
        String token = "invalid-token";
        String voice = "audioData";
        PlayerAudio playerAudio = mock(PlayerAudio.class);
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAudio> payload = mock(TimestampedRequest.class);

        // Setting up nativeHeaders
        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put("receiptId", Collections.singletonList(receiptID));
        nativeHeaders.put("token", Collections.singletonList(token));
        when(headerAccessor.getHeader("nativeHeaders")).thenReturn(nativeHeaders);
        when(payload.getMessage()).thenReturn(playerAudio);
        when(playerAudio.getUserID()).thenReturn(userID);
        when(playerAudio.getAudioData()).thenReturn(voice);

        when(userService.findByToken(token)).thenReturn(false);

        gameController.uploadAudio(headerAccessor, roomID, payload);

        verify(gameService, never()).setPlayerAudio(anyString(), anyString(), anyString());
        verify(socketService, times(1)).broadcastResponse(userID, roomID, false, false, "Invalid or expired token", receiptID);
    }

    @Test
    void testUploadAudio_withException() {
        String roomID = "room1";
        String userID = "user1";
        String receiptID = "receipt1";
        String token = "valid-token";
        String voice = "audioData";
        PlayerAudio playerAudio = mock(PlayerAudio.class);
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        @SuppressWarnings("unchecked")
        TimestampedRequest<PlayerAudio> payload = mock(TimestampedRequest.class);

        // Setting up nativeHeaders
        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put("receiptId", Collections.singletonList(receiptID));
        nativeHeaders.put("token", Collections.singletonList(token));
        when(headerAccessor.getHeader("nativeHeaders")).thenReturn(nativeHeaders);
        when(payload.getMessage()).thenReturn(playerAudio);
        when(playerAudio.getUserID()).thenReturn(userID);
        when(playerAudio.getAudioData()).thenReturn(voice);

        when(userService.findByToken(token)).thenReturn(true);
        doThrow(new RuntimeException("Test exception")).when(gameService).setPlayerAudio(roomID, userID, voice);

        gameController.uploadAudio(headerAccessor, roomID, payload);

        verify(gameService, times(1)).setPlayerAudio(roomID, userID, voice);
        verify(socketService, times(1)).broadcastResponse(userID, roomID, false, true, "Failed to upload audio: Test exception", receiptID);
    }


    @Test
    void testResponse() {
        // Set up the payload
        String payload = "Test payload";

        // Capture System.out output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Call the controller method
        gameController.response(payload);

        // Verify the output
        assertEquals("Test payload\n", outContent.toString());
    }

    @Test
    void testNotifyLobbyInfo() {
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);

        // Capture System.out output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Call the controller method
        gameController.notifyLobbyInfo(headerAccessor);

        // Verify interactions
        verify(socketService, times(1)).broadcastLobbyInfo();
    }

}