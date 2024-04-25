package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class PlayerGetDTO {
    private String userID;
    private String username;
    private String avatar;

    public String getUsername(){
        return this.username;
    }
    public void setUsername(String username) {
        this.username= username;
    }
    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }
    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }



}
