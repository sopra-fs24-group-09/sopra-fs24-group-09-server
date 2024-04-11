package ch.uzh.ifi.hase.soprafs24.entity;
import org.springframework.data.mongodb.core.mapping.Document;

import ch.uzh.ifi.hase.soprafs24.constant.PlayerStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "player") 
public class Player extends User {

    private String audioData; 
    private Integer guessScore = 0;
    private Integer speakScore = 0;
    private boolean ifGuessed = false;
    private PlayerStatus playerStatus= PlayerStatus.UNREADY;

    private List<String> wordsEachRound = new ArrayList<>();
    private List<Map<String, Object>> scoreDetails = new ArrayList<>();

    public Player(User user) {
        this.setId(user.getId());
        this.setUsername(user.getUsername());
        this.setPassword(user.getPassword());
    }

    public List<Map<String, Object>> getScoreDetails() {
        return scoreDetails;
    }

    public void setScoreDetails(List<Map<String, Object>> scoreDetails) {
        this.scoreDetails = scoreDetails;
    }

    public void addScoreDetail(String word, Integer role, Integer score) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("word", word);
        detail.put("role", role);
        detail.put("score", score);
        this.scoreDetails.add(detail);
    }

    public String getAudioData() {
        return audioData;
    }

    public void setAudioData(String audioData) {
        this.audioData = audioData;
    }

    public PlayerStatus getPlayerStatus() {
        return playerStatus;
    }

    public void setPlayerStatus(PlayerStatus playerStatus) {
        this.playerStatus = playerStatus;
    }

    public List<String> getWordsEachRound() {
        return wordsEachRound;
    }

    public void setWordsEachRound(List<String> wordsEachRound) {
        this.wordsEachRound = wordsEachRound;
    }

    public void addWordForRound(String word) {
        this.wordsEachRound.add(word);
    }


    public Integer getGuessScore() {
        return guessScore;
    }

    public void setGuessScore(Integer guessScore) {
        this.guessScore = guessScore;
    }

    public Integer getSpeakScore() {
        return speakScore;
    }

    public void setSpeakScore(Integer speakScore) {
        this.speakScore = speakScore;
    }

    public boolean isIfGuessed() {
        return ifGuessed;
    }

    public void setIfGuessed(boolean ifGuessed) {
        this.ifGuessed = ifGuessed;
    }
    
}
