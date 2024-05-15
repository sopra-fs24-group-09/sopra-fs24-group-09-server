package ch.uzh.ifi.hase.soprafs24.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(player);
        assertEquals(0, player.getTotalScore());
        assertEquals(0, player.getGuessScore());
        assertEquals(0, player.getSpeakScore());
        assertTrue(player.getIfGuessed());
        assertFalse(player.isRoundFinished());
        assertNotNull(player.getScoreDetails());
        assertTrue(player.getScoreDetails().isEmpty());
    }

    @Test
    void testConstructorWithUser() {
        User user = new User();
        user.setId("1");
        user.setUsername("testUser");
        user.setToken("testToken");
        user.setAvatar("testAvatar");

        player = new Player(user);

        assertEquals("1", player.getId());
        assertEquals("testUser", player.getUsername());
        assertEquals("testToken", player.getToken());
        assertEquals("testAvatar", player.getAvatar());
    }

    @Test
    void testSetAndGetAudioData() {
        String audioData = "testAudioData";
        player.setAudioData(audioData);
        assertEquals(audioData, player.getAudioData());
    }

    @Test
    void testSetAndGetAssignedWord() {
        String assignedWord = "testWord";
        player.setAssignedWord(assignedWord);
        assertEquals(assignedWord, player.getAssignedWord());
    }

    @Test
    void testSetAndGetGuessScore() {
        int guessScore = 10;
        player.setGuessScore(guessScore);
        assertEquals(guessScore, player.getGuessScore());
    }

    @Test
    void testSetAndGetSpeakScore() {
        int speakScore = 20;
        player.setSpeakScore(speakScore);
        assertEquals(speakScore, player.getSpeakScore());
    }

    @Test
    void testSetAndGetTotalScore() {
        int totalScore = 30;
        player.setTotalScore(totalScore);
        assertEquals(totalScore, player.getTotalScore());
    }

    @Test
    void testSetAndGetIfGuessed() {
        player.setIfGuessed(false);
        assertFalse(player.getIfGuessed());
    }

    @Test
    void testSetAndGetRoundFinished() {
        player.setRoundFinished(true);
        assertTrue(player.isRoundFinished());
    }

    @Test
    void testSetAndGetScoreDetails() {
        List<Map<String, Object>> scoreDetails = new ArrayList<>();
        Map<String, Object> detail = new HashMap<>();
        detail.put("word", "testWord");
        detail.put("role", 1);
        detail.put("score", 50);
        scoreDetails.add(detail);

        player.setScoreDetails(scoreDetails);
        assertEquals(scoreDetails, player.getScoreDetails());
    }

    @Test
    void testAddScoreDetail() {
        String word = "testWord";
        int role = 1;
        int score = 50;

        player.addScoreDetail(word, role, score);

        List<Map<String, Object>> scoreDetails = player.getScoreDetails();
        assertEquals(1, scoreDetails.size());

        Map<String, Object> detail = scoreDetails.get(0);
        assertEquals(word, detail.get("word"));
        assertEquals(role, detail.get("role"));
        assertEquals(score, detail.get("score"));
    }
}

