package ch.uzh.ifi.hase.soprafs24.entity;
import ch.uzh.ifi.hase.soprafs24.constant.*;
import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;


/**
 * Internal User Representation
 * This class composes the internal representation of the user and defines how
 * the user is stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "ROOM")
public class Room implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "room_sequence")
    @SequenceGenerator(name = "room_sequence", sequenceName = "room_sequence", allocationSize = 1, initialValue = 10001)
    private long roomId;
    @Column()
    private Theme theme;

    @Column(nullable = false)
    private long roomOwnerId;

    @Column()
    private int maxPlayersNum;

    @Column(nullable = false)
    private RoomProperty roomProperty;

    @Column
    @ElementCollection
    private List<Long> roomPlayersList= new ArrayList<>();

    @Column
    @ElementCollection
    private List<Long> alivePlayersList = new ArrayList<>();



    private int currentPlayerIndex = 0; // index inside
    private Long playToOuted = null;

    @ElementCollection
    private Map<Long, Long> votingResult = new HashMap<>();

    public Map<Long, Long> getVotingResult() {
        return votingResult;
    }

    public void setVotingResult(Map<Long, Long> votingResult) {
        this.votingResult = votingResult;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
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

    public void addRoomPlayerList(Long id) {
        if (id!=null) {
            this.roomPlayersList.add(id);
        }
    }

    public List<Long> getRoomPlayersList() {
        return roomPlayersList;
    }

    public void setRoomPlayersList(List<Long> roomPlayersList) {
        this.roomPlayersList = roomPlayersList;
    }

    public long getRoomOwnerId() {
        return roomOwnerId;
    }

    public void setRoomOwnerId(long roomOwnerId) {
        this.roomOwnerId = roomOwnerId;
    }
    public void setMaxPlayersNum(int maxPlayersNum) {
        this.maxPlayersNum = maxPlayersNum;
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
}
