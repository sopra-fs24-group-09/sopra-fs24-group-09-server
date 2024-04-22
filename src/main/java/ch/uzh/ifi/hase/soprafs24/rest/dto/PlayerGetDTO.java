package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class PlayerGetDTO {
    private Long userID;
    private String username;
    private String avatar;

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



}
