package ch.uzh.ifi.hase.soprafs24.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;

public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private SocketService socketService;

    @InjectMocks
    private GameService gameService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
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
        assertTrue(game.getAnsweredPlayerList().contains(player));
        verify(gameRepository).save(game);
        verify(socketService).broadcastGameinfo(game.getRoomId(), "score");
        verify(socketService).broadcastPlayerInfo(game.getRoomId(), "null");
    }





}
