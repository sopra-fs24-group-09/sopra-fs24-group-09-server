package ch.uzh.ifi.hase.soprafs24.model;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AnswerGuessTest {

    @Test
    public void testGettersAndSetters() {
        AnswerGuess answerGuess = new AnswerGuess();

        String userID = "user123";
        String roomID = "room123";
        String guess = "myGuess";
        Long roundNum = 1L;
        String currentSpeakerID = "speaker123";

        // Set values
        answerGuess.setUserID(userID);
        answerGuess.setRoomID(roomID);
        answerGuess.setGuess(guess);
        answerGuess.setRoundNum(roundNum);
        answerGuess.setCurrentSpeakerID(currentSpeakerID);

        // Test getters
        assertEquals(userID, answerGuess.getUserID());
        assertEquals(roomID, answerGuess.getRoomID());
        assertEquals(guess, answerGuess.getGuess());
        assertEquals(roundNum, answerGuess.getRoundNum());
        assertEquals(currentSpeakerID, answerGuess.getCurrentSpeakerID());
    }
}

