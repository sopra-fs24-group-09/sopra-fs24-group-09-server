package ch.uzh.ifi.hase.soprafs24.entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.constant.RoundStatus;
import ch.uzh.ifi.hase.soprafs24.constant.Theme;

import java.util.ArrayList;
import java.util.List;

class GameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(game);
        assertTrue(game.getAnsweredPlayerList().isEmpty());
        assertEquals(0, game.getCurrentRoundNum());
    }

    @Test
    void testConstructorWithRoom() {
        Room room = new Room();
        room.setRoomId("1");
        room.setTheme(Theme.FOOD);
        List<String> players = new ArrayList<>();
        players.add("player1");
        room.setRoomPlayersList(players);

        game = new Game(room);

        assertEquals("1", game.getRoomId());
        assertEquals(Theme.FOOD, game.getTheme());
        assertEquals(players, game.getRoomPlayersList());
    }

    @Test
    void testSetAndGetCurrentSpeaker() {
        Player player = new Player();
        game.setCurrentSpeaker(player);
        assertEquals(player, game.getCurrentSpeaker());
        assertFalse(player.getIfGuessed());
    }

    @Test
    void testSetAndGetAnsweredPlayerList() {
        List<String> answeredPlayerList = new ArrayList<>();
        answeredPlayerList.add("player1");
        game.setAnsweredPlayerList(answeredPlayerList);
        assertEquals(answeredPlayerList, game.getAnsweredPlayerList());
    }

    @Test
    void testSetAndGetCurrentAnswer() {
        String answer = "answer";
        game.setCurrentAnswer(answer);
        assertEquals(answer, game.getCurrentAnswer());
    }

    @Test
    void testSetAndGetRoundStatus() {
        game.setRoundStatus(RoundStatus.guess);
        assertEquals(RoundStatus.guess, game.getRoundStatus());
    }

    @Test
    void testSetAndGetCurrentRoundNum() {
        game.setCurrentRoundNum(5);
        assertEquals(5, game.getCurrentRoundNum());
    }

    @Test
    void testSetAndGetGameStatus() {
        game.setGameStatus(GameStatus.ingame);
        assertEquals(GameStatus.ingame, game.getGameStatus());
    }

    @Test
    void testSetAndGetRoundDue() {
        String roundDue = "2024-05-01T10:00:00Z";
        game.setRoundDue(roundDue);
        assertEquals(roundDue, game.getRoundDue());
    }
}

