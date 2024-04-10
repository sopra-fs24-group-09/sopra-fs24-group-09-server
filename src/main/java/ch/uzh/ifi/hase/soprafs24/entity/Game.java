package ch.uzh.ifi.hase.soprafs24.entity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import ch.uzh.ifi.hase.soprafs24.constant.RoundStatus;

@Document(collection = "game") 
public class Game extends Room{

    private User currentSpeaker;
    private String currentAnswer;
    private RoundStatus roundStatus;
    private int currentRoundNum;

    public Game(Room room) {
        this.setRoomId(room.getRoomId());
        this.setTheme(room.getTheme());
        this.setRoomPlayersList(room.getRoomPlayersList());
    }

    public User getCurrentSpeaker() {
        return currentSpeaker;
    }

    public void setCurrentSpeaker(User currentSpeaker) {
        this.currentSpeaker = currentSpeaker;
    }

    public String getCurrentAnswer() {
        return currentAnswer;
    }

    public void setCurrentAnswer(String currentAnswer) {
        this.currentAnswer = currentAnswer;
    }

    public RoundStatus getRoundStatus() {
        return roundStatus;
    }

    public void setRoundStatus(RoundStatus roundStatus) {
        this.roundStatus = roundStatus;
    }

    public int getCurrentRoundNum() {
        return currentRoundNum;
    }

    public void setCurrentRoundNum(int currentRoundNum) {
        this.currentRoundNum = currentRoundNum;
    }

}
