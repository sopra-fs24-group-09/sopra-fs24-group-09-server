// package ch.uzh.ifi.hase.soprafs24.service;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.Mockito;
// import org.mockito.MockitoAnnotations;
// import org.springframework.web.server.ResponseStatusException;

// import ch.uzh.ifi.hase.soprafs24.constant.PlayerStatus;
// import ch.uzh.ifi.hase.soprafs24.entity.Game;
// import ch.uzh.ifi.hase.soprafs24.entity.Room;
// import ch.uzh.ifi.hase.soprafs24.entity.User;
// import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
// import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
// import ch.uzh.ifi.hase.soprafs24.repository.RoomRepository;
// import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.BDDMockito.*;

// import java.util.Arrays;
// import java.util.Optional;

// public class GameServiceIntegrationTest {

//     @Mock
//     private PlayerRepository playerRepository;
//     @Mock
//     private GameRepository gameRepository;
//     @Mock
//     private PlayerService playerService;
//     @Mock
//     private UserService userService;
//     @Mock
//     private SocketService socketService;
//     @Mock
//     private RoomRepository roomRepository;
//     @Mock
//     private UserRepository userRepository;

//     @InjectMocks
//     private GameService gameService;

//     @BeforeEach
//     public void setup() {
//         MockitoAnnotations.openMocks(this);
//         gameService = Mockito.spy(new GameService(playerRepository, userRepository, gameRepository, userService, socketService, roomRepository));
//     }

//     @Test
//     public void testReady() {
//         // given
//         String userId = "testUserId";
//         User user = new User();
//         user.setId(userId);
//         user.setPlayerStatus(PlayerStatus.UNREADY); // Ensure the user's status is initially UNREADY

//         given(userService.findUserById(userId)).willReturn(user);

//         // when
//         gameService.Ready(userId);

//         // then
//         verify(userService).findUserById(userId);
//         assertEquals(PlayerStatus.READY, user.getPlayerStatus(), "User should be set to READY status");
//     }

//     @Test
//     public void testUnready() {
//         // given
//         String userId = "testUserId";
//         User user = new User();
//         user.setId(userId);
//         user.setPlayerStatus(PlayerStatus.READY); // Ensure the user's status is initially READY

//         given(userService.findUserById(userId)).willReturn(user);

//         // when
//         gameService.UnReady(userId);

//         // then
//         verify(userService).findUserById(userId);
//         assertEquals(PlayerStatus.UNREADY, user.getPlayerStatus(), "User should be set to UNREADY status");
//     }

//     @Test
//     public void whenNotAllPlayersReady_shouldThrowException() {
//         Room room = new Room();
//         room.setRoomPlayersList(Arrays.asList("user1", "user2"));
//         room.setMaxPlayersNum(2);
//         User user1 = new User();
//         user1.setId("user1");
//         user1.setPlayerStatus(PlayerStatus.READY);
//         User user2 = new User();
//         user2.setId("user2");
//         user2.setPlayerStatus(PlayerStatus.UNREADY);

//         given(roomRepository.findByRoomId("roomId")).willReturn(Optional.of(room));


//         when(userService.findUserById("user1")).thenReturn(user1);
//         when(userService.findUserById("user2")).thenReturn(user2);

//         assertThrows(ResponseStatusException.class, () -> gameService.checkIfAllReady(room));
//         verify(gameRepository, never()).save(any(Game.class));

//     }

//     @Test
//     public void whenAllPlayersReady_shouldStartGameSuccessfully() {
//         // Create a room and mock its properties
//         Room room = new Room();
//         room.setRoomId("roomId");
//         room.setRoomPlayersList(Arrays.asList("user1", "user2"));
    
//         // Mock users to be READY
//         User user1 = new User();
//         user1.setId("user1");
//         user1.setPlayerStatus(PlayerStatus.READY);

//         User user2 = new User();
//         user2.setId("user2");
//         user2.setPlayerStatus(PlayerStatus.READY);
    
//         when(userService.findUserById("user1")).thenReturn(user1);
//         when(userService.findUserById("user2")).thenReturn(user2);
    
//         // Mock the room repository to return the configured room
//         when(roomRepository.findById("roomId")).thenReturn(Optional.of(room));
    
//         // Mock the startGame method to check it is called
//         doNothing().when(gameService).startGame(room);
    
//         // Execute the method under test
//         assertDoesNotThrow(() -> gameService.checkIfAllReady(room),
//                            "Should not throw an exception as all players are READY");
    
//         // Verify that the startGame method was indeed called, indicating progression to game start
//         verify(gameService).startGame(room);
//     }
    

// }
