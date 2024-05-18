package ch.uzh.ifi.hase.soprafs24.service;
import static org.mockito.ArgumentMatchers.anyString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.constant.PlayerStatus;
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

public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserService userService;

    @Mock
    private SocketService socketService;


    @InjectMocks
    private GameService gameService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); 
        gameService = Mockito.spy(new GameService(playerRepository, userRepository, gameRepository, userService, socketService, roomRepository));
    }

  
    @Test
    public void speakPhase_shouldSetupAndBroadcastSpeakInfo() {
        Game game = new Game();
        User user = new User();
        game.setRoomId("room1");
        game.setCurrentSpeaker(new Player(user));

        gameService.speakPhase(game);

        verify(gameRepository).save(game);
        verify(socketService).broadcastGameinfo(game.getRoomId(), "speak");
        verify(socketService).broadcastPlayerInfo(game.getRoomId(), "speak");
    }

    @Test
    public void guessPhase_shouldSetupAndBroadcastGuessInfo() {
        Game game = new Game();
        game.setRoomId("room1");

        gameService.guessPhase(game);

        verify(gameRepository).save(game);
        verify(socketService).broadcastGameinfo(game.getRoomId(), "guess");
        verify(socketService).broadcastPlayerInfo(game.getRoomId(), "guess");
    }

    @Test
    public void revealPhase_shouldSetupAndBroadcastRevealInfo() {
        Game game = new Game();
        game.setRoomId("room1");

        gameService.revealPhase(game);

        verify(gameRepository).save(game);
        verify(socketService).broadcastGameinfo(game.getRoomId(), "reveal");
        verify(socketService).broadcastPlayerInfo(game.getRoomId(), "null");
    }

    @Test
    public void validateAnswer_whenGuessIsCorrect_shouldUpdateGameAndPlayer() {
        Game game = new Game();
        game.setRoomId("room1");
        game.setCurrentAnswer("correct");

        Player player = new Player();
        player.setId("player1");

        String guess = "correct";

        // Execute
        gameService.validateAnswer(game, player, guess);

        // Verify
        assertTrue(player.isRoundFinished());
        assertTrue(game.getAnsweredPlayerList().contains(player.getId()));
        verify(gameRepository).save(game);
        verify(socketService).broadcastGameinfo(game.getRoomId(), "score");
        verify(socketService).broadcastPlayerInfo(game.getRoomId(), "null");
    }

    @Test
    public void validateAnswer_whenGuessIsIncorrect_shouldUpdateGameAndPlayer() {
        Game game = new Game();
        game.setRoomId("room1");
        game.setCurrentAnswer("correct");

        Player player = new Player();
        player.setId("player1");

        String guess = "incorrect";

        // Execute
        assertThrows(RuntimeException.class, () -> {
            gameService.validateAnswer(game, player, guess);
        }, "Player not found");

    }

    @Test
    public void displayRoundScores_ShouldInvokeGetScoreDetails() {

        // Create a mocked Player object
        Player player = mock(Player.class);

        // Call the method under test
        gameService.displayRoundScores(player);

        // Verify that getScoreDetails was called on the mocked Player object
        verify(player).getScoreDetails();
    }

    @Test
    void displayScores_ShouldUpdateGameAndBroadcast() {
        Game game = new Game();
        game.setRoomId("room1");

        gameService.displayScores(game);

        verify(gameRepository).save(game);
        assertEquals(RoundStatus.reveal, game.getRoundStatus());
        assertEquals(GameStatus.over, game.getGameStatus());
        verify(socketService).broadcastGameinfo(game.getRoomId(), "score");
        verify(socketService).broadcastPlayerInfo(game.getRoomId(), null);
    }

    @Test
    void findGameById_ShouldReturnGameIfExists() {
        String roomId = "room1";
        Game game = new Game();
        when(gameRepository.findById(roomId)).thenReturn(Optional.of(game));

        Game foundGame = gameService.findGameById(roomId);

        assertNotNull(foundGame);
    }

    @Test
    void findGameById_ShouldThrowExceptionIfNotFound() {
        String roomId = "room2";
        when(gameRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> gameService.findGameById(roomId));

        verify(gameRepository).findById(roomId);
    }
    @Test
    public void proceedTurn_ShouldHandleGamePhasesProperly() {
        // Setup
        Game game = new Game();
        game.setRoomId("gameRoomId");
        game.setGameStatus(GameStatus.ingame);
        game.setRoundStatus(RoundStatus.speak);
        game.setCurrentRoundNum(1);
        game.setRoomPlayersList(Arrays.asList("speakerId", "speakerId", "speakerId"));
        
        Player currentSpeaker = new Player();
        currentSpeaker.setId("speakerId");
        currentSpeaker.setAudioData("audio"); // Ensure this is not empty
        currentSpeaker.setAssignedWord("sampleWord"); // Assuming getAssignedWord() needs this


        game.setCurrentSpeaker(currentSpeaker); 
    
        // Stubbing repository and service calls
        when(gameRepository.findByRoomId("gameRoomId")).thenReturn(Optional.of(game));
        when(playerRepository.findById("speakerId")).thenReturn(Optional.of(currentSpeaker));
        when(gameRepository.save(any(Game.class))).thenReturn(game);
    
        // Call the method under test
        gameService.proceedTurn(game);
    
        // Verify the method interactions 
        // Verify that the speakPhase method is called
        verify(gameService).speakPhase(game);
    
        // Verify that the audio is broadcasted
        verify(socketService).broadcastSpeakerAudio(game.getRoomId(), game.getCurrentSpeaker().getId(), "audio");

    
        // Verify that the guessPhase method is called
        verify(gameService).guessPhase(game);
    
        // Verify that the score calculation is triggered
        verify(gameService).calculateScore(game);
    
        // Verify that the reveal phase is handled
        verify(gameService).revealPhase(game);
    }

    @Test
    public void proceedTurn_ShouldHandleGamePhasesProperly_withoutVoice() {
        // Setup
        Game game = new Game();
        game.setRoomId("gameRoomId");
        game.setGameStatus(GameStatus.ingame);
        game.setRoundStatus(RoundStatus.speak);
        game.setCurrentRoundNum(1);
        game.setRoomPlayersList(Arrays.asList("speakerId", "speakerId", "speakerId"));
        
        Player currentSpeaker = new Player();
        currentSpeaker.setId("speakerId");
        currentSpeaker.setAudioData(null); // Ensure this is not empty
        currentSpeaker.setAssignedWord("sampleWord"); // Assuming getAssignedWord() needs this


        game.setCurrentSpeaker(currentSpeaker); 
    
        // Stubbing repository and service calls
        when(gameRepository.findByRoomId("gameRoomId")).thenReturn(Optional.of(game));
        when(playerRepository.findById("speakerId")).thenReturn(Optional.of(currentSpeaker));
        when(gameRepository.save(any(Game.class))).thenReturn(game);
    
        // Call the method under test
        gameService.proceedTurn(game);
    
        // Verify the method interactions 
        // Verify that the speakPhase method is called
        verify(gameService).speakPhase(game);
        verify(gameService, times(0)).guessPhase(game);
    
        // Verify that the score calculation is triggered
        verify(gameService).calculateScore(game);
        // Verify that the reveal phase is handled
        verify(gameService).revealPhase(game);
    }

    @Test
    public void calculateScore_ShouldUpdatePlayerScores() {
        // Setup

        Player currentSpeaker = new Player();
        currentSpeaker.setId("player1");
        currentSpeaker.setAudioData(null); // Ensure this is not empty
        currentSpeaker.setAssignedWord("word1"); // Assuming getAssignedWord() needs this

        Game game = new Game();
        game.setRoomId("gameRoomId");
        game.setGameStatus(GameStatus.ingame);
        game.setRoundStatus(RoundStatus.speak);
        game.setCurrentRoundNum(1);
        game.setRoomPlayersList(Arrays.asList("player1", "player2", "player3"));
        game.setCurrentSpeaker(currentSpeaker);
    
        Player player1 = new Player();
        player1.setId("player1");
        player1.setAssignedWord("word1");
        player1.setRoundFinished(true);
    
        Player player2 = new Player();
        player2.setId("player2");
        player2.setAssignedWord("word2");
        player2.setRoundFinished(true);
    
        Player player3 = new Player();
        player3.setId("player3");
        player3.setAssignedWord("word3");
        player3.setRoundFinished(true);
    
        game.setRoomPlayersList(Arrays.asList(player1.getId(), player2.getId(), player3.getId()));
    
        // Stubbing repository and service calls
        when(gameRepository.findByRoomId("gameRoomId")).thenReturn(Optional.of(game));
        when(playerRepository.findById("player1")).thenReturn(Optional.of(player1));
        when(playerRepository.findById("player2")).thenReturn(Optional.of(player2));
        when(playerRepository.findById("player3")).thenReturn(Optional.of(player3));
        when(gameRepository.save(any(Game.class))).thenReturn(game);
    
        // Call the method under test
        gameService.calculateScore(game);

        // Verify the method interactions
        // Verify that the player scores are updated
        verify(playerRepository, times(2)).save(player1);
        verify(playerRepository, times(2)).save(player2);
        verify(playerRepository, times(2)).save(player3);
    
        // Verify that the game is saved
    }

    @Test
    public void testReady_UserShouldBeReady() {
        // Setup
        User user = new User();
        user.setId("user1");
        user.setPlayerStatus(PlayerStatus.READY);

        // Arrange
        when(userService.findUserById("user1")).thenReturn(user);
    
        // Act
        gameService.Ready("user1", "room1");
    
        // Assert
        assertEquals(PlayerStatus.READY, user.getPlayerStatus());
        verify(userRepository).save(user);
    }

    @Test
    public void testUnready_UserShouldBeUnready() {
        // Setup
        User user = new User();
        user.setId("user1");
        user.setPlayerStatus(PlayerStatus.UNREADY);

        // Arrange
        when(userService.findUserById("user1")).thenReturn(user);
    
        // Act
        gameService.UnReady("user1");
    
        // Assert
        assertEquals(PlayerStatus.UNREADY, user.getPlayerStatus());
        verify(userRepository).save(user);
    }


    @Test
    void checkIfAllReady_lessThanTwoPlayers_throwsException() {
        Room room = new Room();
        room.setRoomPlayersList(Arrays.asList("player1"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameService.checkIfAllReady(room);
        });

        assertEquals("2 or more players are required to start the game", exception.getMessage());
    }

    @Test
    void checkIfAllReady_gameAlreadyStarted_throwsException() {
        Room room = new Room();
        room.setRoomPlayersList(Arrays.asList("player1", "player2"));
        room.setRoomProperty(RoomProperty.INGAME);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameService.checkIfAllReady(room);
        });

        assertEquals("The game has already been started for this room", exception.getMessage());
    }

    @Test
    void checkIfAllReady_notAllPlayersReady_throwsException() {
        Room room = new Room();
        room.setRoomPlayersList(Arrays.asList("player1", "player2"));
        room.setRoomProperty(RoomProperty.WAITING);

        User player1 = new User();
        player1.setId("player1");
        player1.setPlayerStatus(PlayerStatus.READY);

        User player2 = new User();
        player2.setId("player2");
        player2.setPlayerStatus(PlayerStatus.UNREADY);

        when(userService.findUserById("player1")).thenReturn(player1);
        when(userService.findUserById("player2")).thenReturn(player2);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameService.checkIfAllReady(room);
        });

        assertEquals("Not all players are ready", exception.getMessage());
    }

    @Test
    public void testStartGame_WithNullTheme_ThrowsException() {
        Room room = new Room();
        Game game = new Game(room);
        game.setTheme(null);  // Intentionally setting theme to null

        Exception exception = assertThrows(IllegalStateException.class, () -> gameService.startGame(room));
        assertEquals("Game theme is not set", exception.getMessage());
    }

    @Test
    public void testStartGame_SetsRoomInGameAndSaves() throws IOException {
        // Prepare the environment
        Room room = new Room();
        room.setRoomId("1");
        room.setTheme(Theme.FOOD);
        Game game = new Game(room);
        game.setGameStatus(GameStatus.ingame);
        game.setRoomPlayersList(Arrays.asList("player1", "player2", "player3"));
    
        // Mock external method calls
//        when(gameService.getWords(anyString())).thenReturn(Arrays.asList("word1", "word2", "word3"));
        when(gameRepository.findByRoomId("gameRoomId")).thenReturn(Optional.of(game));  // Ensure this is returned before calling startGame
    
        // Execute the method under test
        gameService.startGame(room);
    
        // Verifications
        verify(roomRepository, times(2)).save(room);
        verify(socketService, atLeastOnce()).broadcastGameinfo(any(), any());
        verify(socketService, atLeastOnce()).broadcastPlayerInfo(any(), any());
    
        // This will check if endGame correctly handles the Optional
        verify(gameRepository, atLeastOnce()).findByRoomId(any()); // Ensure this is also called by endGame
    }


    @Test
    public void testStartGame_success() throws IOException {
        // Prepare the environment
        Room room = new Room();
        room.setRoomId("1");
        room.setTheme(Theme.FOOD);
        room.setRoomPlayersList(Arrays.asList("player1", "player2", "player3"));
        room.setRoomWordsList(Arrays.asList("word1", "word2", "word3"));
    
        Game game = new Game(room);
        game.setGameStatus(GameStatus.ingame);
        game.setRoomPlayersList(Arrays.asList("player1", "player2", "player3"));

    
        User player1 = new User();
        player1.setId("player1");
        User player2 = new User();
        player2.setId("player2");
        User player3 = new User();
        player3.setId("player3");
        
    
        Player p1 = new Player(player1);
        Player p2 = new Player(player2);
        Player p3 = new Player(player3);

        game.setCurrentSpeaker(p1);
    
        // Mock external method calls
//        when(gameService.getWords(anyString())).thenReturn(Arrays.asList("word1", "word2", "word3"));
        when(userService.findUserById("player1")).thenReturn(player1);
        when(userService.findUserById("player2")).thenReturn(player2);
        when(userService.findUserById("player3")).thenReturn(player3);
        when(playerRepository.findById("player1")).thenReturn(Optional.of(p1));
        when(playerRepository.findById("player2")).thenReturn(Optional.of(p2));
        when(playerRepository.findById("player3")).thenReturn(Optional.of(p3));
        when(gameRepository.findByRoomId("1")).thenReturn(Optional.of(game));
        when(userRepository.findById("player1")).thenReturn(Optional.of(player1));
        when(userRepository.findById("player2")).thenReturn(Optional.of(player2));
        when(userRepository.findById("player3")).thenReturn(Optional.of(player3));
        when(roomRepository.findById("1")).thenReturn(Optional.of(room));
    
        // Execute the method under test
        gameService.startGame(room);
    
        // Verifications
        verify(roomRepository, times(2)).save(room);
        verify(socketService, atLeastOnce()).broadcastGameinfo(any(), any());
        verify(socketService, atLeastOnce()).broadcastPlayerInfo(any(), any());
        verify(playerRepository, atLeastOnce()).save(p1);
        verify(playerRepository, atLeastOnce()).save(p2);
        verify(playerRepository, atLeastOnce()).save(p3);
    }

    @Test
    public void testSetPlayerAudio_GameNotFound() {
        String roomId = "room1";
        String playerId = "player1";
        String voice = "audio data";

        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gameService.setPlayerAudio(roomId, playerId, voice);
        });

        assertEquals("Game not found with ID: " + roomId, exception.getMessage());
    }

    @Test
    public void testSetPlayerAudio_PlayerInList_SetsAudioData() {
        String roomId = "room1";
        String playerId = "player1";
        String voice = "audio data";
    
        Game game = new Game();
        game.setRoomId(roomId);
        game.setRoomPlayersList(Arrays.asList(playerId, "player2"));
    
        Player player = new Player();
        player.setId(playerId);
    
        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.of(game));
        when(playerRepository.findById(anyString())).thenReturn(Optional.of(player));
    
        gameService.setPlayerAudio(roomId, playerId, voice);
    
        assertEquals(voice, player.getAudioData());
        verify(playerRepository, times(1)).save(player);
        verify(socketService, times(1)).broadcastPlayerInfo(roomId, "audio");
    }

    @Test
    public void testSetPlayerAudio_RoundStatusGuess_BroadcastsSpeakerAudio() {
        String roomId = "room1";
        String playerId = "player1";
        String voice = "audio data";
    
        Game game = new Game();
        game.setRoomId(roomId);
        game.setRoomPlayersList(Arrays.asList(playerId, "player2"));
        game.setRoundStatus(RoundStatus.guess);
    
        Player player = new Player();
        player.setId(playerId);
    
        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.of(game));
        when(playerRepository.findById(anyString())).thenReturn(Optional.of(player));
    
        gameService.setPlayerAudio(roomId, playerId, voice);
    
        assertEquals(voice, player.getAudioData());
        verify(playerRepository, times(1)).save(player);
        verify(socketService, times(1)).broadcastSpeakerAudio(roomId, playerId, voice);
        verify(socketService, times(1)).broadcastPlayerInfo(roomId, "audio");
    }


}


