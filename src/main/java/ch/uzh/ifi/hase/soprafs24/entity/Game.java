package ch.uzh.ifi.hase.soprafs24.entity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import ch.uzh.ifi.hase.soprafs24.constant.RoundStatus;

@Document(collection = "game") 
public class Game {
    
    @Id
    private String roomId;
    private List<Player> playerList;
    private Player currentSpeaker;
    private String currentAnswer;
    private RoundStatus roundStatus;
    private int currentRoundNum;
    private Map<String, Integer> playerScores = new HashMap<>();

    public Game() {
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Player getCurrentSpeaker() {
        return currentSpeaker;
    }

    public void setCurrentSpeaker(Player currentSpeaker) {
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


    public Map<String, Integer> getPlayerScores() {
        return playerScores;
    }

    public void setPlayerScores(Map<String, Integer> playerScores) {
        this.playerScores = playerScores;
    }

    public void updatePlayerScore(String playerId, int score) {
        int currentScore = playerScores.getOrDefault(playerId, 0);
        playerScores.put(playerId, currentScore + score);
    }

    public List<Player> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(List<Player> playerList) {
        this.playerList = playerList;
    }

    public void addPlayer(Player player) {
        this.playerList.add(player);
    }

}
