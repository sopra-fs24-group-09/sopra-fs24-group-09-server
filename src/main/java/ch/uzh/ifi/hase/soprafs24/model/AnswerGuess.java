package ch.uzh.ifi.hase.soprafs24.model;

public class AnswerGuess {
    private String userId;
    private String roomID;
    private String guess;
    private Long roundNum;
    private String currentSpeakerId;
    
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getRoomId() {
        return roomID;
    }
    public void setRoomId(String roomId) {
        this.roomID = roomId;
    }
    public String getGuess() {
        return guess;
    }
    public void setGuess(String guess) {
        this.guess = guess;
    }
    public Long getRoundNum() {
        return roundNum;
    }
    public void setRoundNum(Long roundNum) {
        this.roundNum = roundNum;
    }
    public String getCurrentSpeakerId() {
        return currentSpeakerId;
    }
    public void setCurrentSpeakerId(String currentSpeakerId) {
        this.currentSpeakerId = currentSpeakerId;
    }

}
