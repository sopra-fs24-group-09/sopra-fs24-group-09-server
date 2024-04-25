package ch.uzh.ifi.hase.soprafs24.model;

public class AnswerGuess {
    private String userID;
    private String roomID;
    private String guess;
    private Long roundNum;
    private String currentSpeakerID;
    
    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }
    public String getRoomID() {
        return roomID;
    }
    public void setRoomID(String roomID) {
        this.roomID = roomID;
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
    public String getCurrentSpeakerID() {
        return currentSpeakerID;
    }
    public void setCurrentSpeakerID(String currentSpeakerID) {
        this.currentSpeakerID = currentSpeakerID;
    }

}
