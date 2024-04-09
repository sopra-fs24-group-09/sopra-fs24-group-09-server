package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.RoomProperty;
import ch.uzh.ifi.hase.soprafs24.constant.Theme;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.io.Serializable;

@Document(collection = "room") // 指定这是一个MongoDB文档，并可以指定集合名称
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String roomId;

    private Theme theme;

    private User roomOwner;

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

    public User getRoomOwner() {
        return roomOwner;
    }

    public void setRoomOwner(User roomOwner) {
        this.roomOwner = roomOwner;
    }


}
