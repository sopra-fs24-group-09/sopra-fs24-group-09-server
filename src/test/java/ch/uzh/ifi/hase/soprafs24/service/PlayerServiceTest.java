//
//package ch.uzh.ifi.hase.soprafs24.service;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//import ch.uzh.ifi.hase.soprafs24.constant.PlayerStatus;
//import ch.uzh.ifi.hase.soprafs24.entity.Player;
//import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//public class PlayerServiceTest {
//
//    @Mock
//    private PlayerRepository playerRepository;
//
//    @InjectMocks
//    private PlayerService playerService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    public void getPlayers_ShouldReturnAllPlayers() {
//        Player player1 = new Player();
//        Player player2 = new Player();
//        when(playerRepository.findAll()).thenReturn(Arrays.asList(player1, player2));
//
//        List<Player> players = playerService.getPlayers();
//
//        assertNotNull(players);
//        assertEquals(2, players.size());
//        verify(playerRepository).findAll();
//    }
//
//    @Test
//    public void createPlayer_ShouldCreatePlayerWithInitialSetup() {
//        Player newPlayer = new Player();
//        newPlayer.setUsername("Test Player");
//
//        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        Player savedPlayer = playerService.createPlayer(newPlayer);
//
//        assertNotNull(savedPlayer.getToken());
//        assertEquals(PlayerStatus.UNREADY, savedPlayer.getPlayerStatus());
//        verify(playerRepository, times(2)).save(savedPlayer);  // Called twice in the method
//    }
//
//}
