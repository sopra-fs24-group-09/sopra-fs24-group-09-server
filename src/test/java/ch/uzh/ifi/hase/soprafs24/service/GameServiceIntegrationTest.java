package ch.uzh.ifi.hase.soprafs24.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.constant.PlayerStatus;
import ch.uzh.ifi.hase.soprafs24.constant.Theme;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class GameServiceIntegrationTest {

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private PlayerService playerService;
    @Mock
    private UserService userService;
    @Mock
    private SocketService socketService;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GameService gameService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        gameService = Mockito.spy(new GameService(playerRepository, userRepository, gameRepository, userService, socketService, roomRepository));
    }

    @Test
    public void testReady() {
        // given
        String userId = "testUserId";
        User user = new User();
        user.setId(userId);
        user.setPlayerStatus(PlayerStatus.UNREADY); // Ensure the user's status is initially UNREADY

        given(userService.findUserById(userId)).willReturn(user);

        // when
        gameService.Ready(userId, "roomId");

        // then
        verify(userService).findUserById(userId);
        assertEquals(PlayerStatus.READY, user.getPlayerStatus(), "User should be set to READY status");
    }

    @Test
    public void testUnready() {
        // given
        String userId = "testUserId";
        User user = new User();
        user.setId(userId);
        user.setPlayerStatus(PlayerStatus.READY); // Ensure the user's status is initially READY

        given(userService.findUserById(userId)).willReturn(user);

        // when
        gameService.UnReady(userId);

        // then
        verify(userService).findUserById(userId);
        assertEquals(PlayerStatus.UNREADY, user.getPlayerStatus(), "User should be set to UNREADY status");
    }

    @Test
    public void whenNotAllPlayersReady_shouldThrowException() {
        Room room = new Room();
        room.setRoomPlayersList(Arrays.asList("user1", "user2"));
        room.setMaxPlayersNum(2);
        User user1 = new User();
        user1.setId("user1");
        user1.setPlayerStatus(PlayerStatus.READY);
        User user2 = new User();
        user2.setId("user2");
        user2.setPlayerStatus(PlayerStatus.UNREADY);

        given(roomRepository.findByRoomId("roomId")).willReturn(Optional.of(room));


        when(userService.findUserById("user1")).thenReturn(user1);
        when(userService.findUserById("user2")).thenReturn(user2);

        assertThrows(RuntimeException.class, () -> gameService.checkIfAllReady(room));
        verify(gameRepository, never()).save(any(Game.class));

    }

    @Test
    public void whenAllPlayersReady_shouldStartGameSuccessfully() {
        // Create a room and mock its properties
        Room room = new Room();
        room.setRoomId("roomId");
        room.setRoomPlayersList(Arrays.asList("user1", "user2"));
    
        // Mock users to be READY
        User user1 = new User();
        user1.setId("user1");
        user1.setPlayerStatus(PlayerStatus.READY);

        User user2 = new User();
        user2.setId("user2");
        user2.setPlayerStatus(PlayerStatus.READY);
    
        when(userService.findUserById("user1")).thenReturn(user1);
        when(userService.findUserById("user2")).thenReturn(user2);
    
        // Mock the room repository to return the configured room
        when(roomRepository.findById("roomId")).thenReturn(Optional.of(room));
    
        // Mock the startGame method to check it is called
        doNothing().when(gameService).startGame(room);
    
        // Execute the method under test
        assertDoesNotThrow(() -> gameService.checkIfAllReady(room),
                           "Should not throw an exception as all players are READY");
    
    }

    @Test
    public void testStartGame_success() throws IOException {
        // Prepare the environment
        Room room = new Room();
        room.setRoomId("1");
        room.setTheme(Theme.FOOD);
        room.setRoomPlayersList(Arrays.asList("player1", "player2", "player3"));
    
        Game game = new Game(room);
        game.setGameStatus(GameStatus.ingame);
        game.setRoomPlayersList(Arrays.asList("player1", "player2", "player3"));
        room.setRoomWordsList(Arrays.asList("word1", "word2", "word3"));
        game.setRoomId("1");

    
        User player1 = new User();
        player1.setId("player1");
        player1.setInRoomId("1");
        User player2 = new User();
        player2.setId("player2");
        player2.setInRoomId("1");
        User player3 = new User();
        player3.setId("player3");
        player3.setInRoomId("1");
        
    
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
        when(roomRepository.findByRoomId("1")).thenReturn(Optional.of(room));
        doNothing().when(gameRepository).delete(game);
    
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

    

}
