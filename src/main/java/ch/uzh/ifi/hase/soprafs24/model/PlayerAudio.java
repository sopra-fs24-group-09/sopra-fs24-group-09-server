package ch.uzh.ifi.hase.soprafs24.model;

public class PlayerAudio {
    private String userId; 
    private String roomId;
    
    public String getRoomId() {
        return roomId;
    }
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    private String audioData;

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getAudioData() {
        return audioData;
    }
    public void setAudioData(String audioData) {
        this.audioData = audioData;
    }

    
}
