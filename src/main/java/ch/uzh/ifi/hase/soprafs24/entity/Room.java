package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.RoomProperty;
import ch.uzh.ifi.hase.soprafs24.constant.Theme;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "room") // 指定这是一个MongoDB文档，并可以指定集合名称
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String roomId;
    private String roomName;
    private Theme theme;
    private User roomOwner;
    private int maxPlayersNum;
    private String roomOwnerId;
    private RoomProperty roomProperty;
    private List<String> roomPlayersList = new ArrayList<>();
    private List<String> roomWordsList = new ArrayList<>();

    public List<String> getRoomWordsList() {
        return roomWordsList;
    }

    public void setRoomWordsList(List<String> roomWordsList) {
        this.roomWordsList = roomWordsList;
    }

    //    private int currentPlayerIndex = 0; // index inside

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

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public User getRoomOwner() {
        return roomOwner;
    }

    public void setRoomOwner(User roomOwner) {
        this.roomOwner = roomOwner;
    }

    public int getMaxPlayersNum() {
        return maxPlayersNum;
    }

    public void setMaxPlayersNum(int maxPlayersNum) {
        this.maxPlayersNum = maxPlayersNum;
    }

    public RoomProperty getRoomProperty() {
        return roomProperty;
    }

    public void setRoomProperty(RoomProperty roomProperty) {
        this.roomProperty = roomProperty;
    }

    public List<String> getRoomPlayersList() {
        return roomPlayersList;
    }

    public void setRoomPlayersList(List<String> roomPlayersList) {
        this.roomPlayersList = roomPlayersList;
    }

//    public int getCurrentPlayerIndex() {
//        return currentPlayerIndex;
//    }
//
//    public void setCurrentPlayerIndex(int currentPlayerIndex) {
//        this.currentPlayerIndex = currentPlayerIndex;
//    }

    
    public void addRoomPlayerList(String id) {
        if (!roomPlayersList.contains(id)) {
            roomPlayersList.add(id);
            System.out.println("➕"+id + " has been added to the room.");
        } else {
            System.out.println(id + " is already in the room.");
        }
}

}
