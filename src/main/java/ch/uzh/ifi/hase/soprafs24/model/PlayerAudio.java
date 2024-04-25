package ch.uzh.ifi.hase.soprafs24.model;

public class PlayerAudio {
    private String userID;
    private String roomID;
    
    public String getRoomID() {
        return roomID;
    }
    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }
    private String audioData;

    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }
    public String getAudioData() {
        return audioData;
    }
    public void setAudioData(String audioData) {
        this.audioData = audioData;
    }

    
}
