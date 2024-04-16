package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.List;
import java.util.Map;

public class PlayerGetDTO {
    private Long userID;
    private String username;
    private String avatar;
    private List<Map<String, Object>> score;
    private boolean ready;
    private boolean ifGuessed;
    private boolean roundFinished;


    public List<Map<String, Object>> getScore() {
        return score;
    }
    public void setScore(List<Map<String, Object>> score) {
        this.score = score;
    }
    public String getUsername(){
        return this.username;
    }
    public void setUsername(String username) {
        this.username= username;
    }
    public Long getUserID() {
        return userID;
    }
    public void setUserID(Long userID) {
        this.userID = userID;
    }
    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public boolean isReady() {
        return ready;
    }
    public void setReady(boolean ready) {
        this.ready = ready;
    }
    public boolean isIfGuessed() {
        return ifGuessed;
    }
    public void setIfGuessed(boolean ifGuessed) {
        this.ifGuessed = ifGuessed;
    }
    public boolean isRoundFinished() {
        return roundFinished;
    }
    public void setRoundFinished(boolean roundFinished) {
        this.roundFinished = roundFinished;
    }


}
