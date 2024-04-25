package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.RoomProperty;
import ch.uzh.ifi.hase.soprafs24.constant.Theme;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.User;

import java.util.List;

public class RoomPostDTO {

    private String roomName;
    private String roomId;
    private Theme theme;
    private RoomProperty roomProperty;
    private int maxPlayersNum;

    private User roomOwner;

    private String roomOwnerId;
    private List<String> roomPlayersList;


    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public User getRoomOwner() {
        return roomOwner;
    }

    public void setRoomOwnerId(User roomOwner) {
        this.roomOwner = roomOwner;
    }

    /*public ArrayList<User> getRoomPlayers() {
        return roomPlayers;
    }*/

    /*public void setRoomPlayers(ArrayList<User> roomPlayers) {
        this.roomPlayers = roomPlayers;
    }*/

    //private ArrayList<User> roomPlayers;


    public String getRoomOwnerId() {
        return roomOwnerId;
    }

    public void setRoomOwnerId(String roomOwnerId) {
        this.roomOwnerId = roomOwnerId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public RoomProperty getRoomProperty() {
        return roomProperty;
    }

    public void setRoomProperty(RoomProperty roomProperty) {
        this.roomProperty = roomProperty;
    }

    public int getMaxPlayersNum() {
        return maxPlayersNum;
    }

    public void setMaxPlayersNum(int maxPlayersNum) {
        this.maxPlayersNum = maxPlayersNum;
    }

    public List<String> getRoomPlayersList() {
        return roomPlayersList;
    }

    public void setRoomPlayersList(List<String> roomPlayersList) {
        this.roomPlayersList = roomPlayersList;
    }
}
