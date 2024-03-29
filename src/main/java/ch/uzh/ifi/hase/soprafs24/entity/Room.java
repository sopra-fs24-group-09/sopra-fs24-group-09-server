package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.RoomProperty;
import ch.uzh.ifi.hase.soprafs24.constant.Theme;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "room") // 指定这是一个MongoDB文档，并可以指定集合名称
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String roomId;

    private Theme theme;

    private long roomOwnerId;

    private int maxPlayersNum;

    private RoomProperty roomProperty;

    private List<Long> roomPlayersList = new ArrayList<>();

    private List<Long> alivePlayersList = new ArrayList<>();

    private int currentPlayerIndex = 0; // index inside
    private Long playToOuted = null;

    private Map<Long, Long> votingResult = new HashMap<>();

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

    public long getRoomOwnerId() {
        return roomOwnerId;
    }

    public void setRoomOwnerId(long roomOwnerId) {
        this.roomOwnerId = roomOwnerId;
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

    public List<Long> getRoomPlayersList() {
        return roomPlayersList;
    }

    public void setRoomPlayersList(List<Long> roomPlayersList) {
        this.roomPlayersList = roomPlayersList;
    }

    public List<Long> getAlivePlayersList() {
        return alivePlayersList;
    }

    public void setAlivePlayersList(List<Long> alivePlayersList) {
        this.alivePlayersList = alivePlayersList;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public Long getPlayToOuted() {
        return playToOuted;
    }

    public void setPlayToOuted(Long playToOuted) {
        this.playToOuted = playToOuted;
    }

    public Map<Long, Long> getVotingResult() {
        return votingResult;
    }

    public void setVotingResult(Map<Long, Long> votingResult) {
        this.votingResult = votingResult;
    }
}
