package ch.uzh.ifi.hase.soprafs24.service;
import ch.uzh.ifi.hase.soprafs24.constant.RoomProperty;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class RoomService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final RoomRepository roomRepository;
    private GameService gameService;
    public RoomService(@Qualifier("roomRepository") RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> getRooms() {
        return this.roomRepository.findAll();
    }

    //Here we create a new room, and we need to set the room property and theme according to the input from client
    public Room createRoom(Room newRoom){
        Optional<Room> existingRoom = roomRepository.findByRoomId(newRoom.getRoomId());
        if (existingRoom.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room already exists");
        }

        try {
            newRoom.setRoomProperty(RoomProperty.WAITING);
            Player roomOwner = new Player(newRoom.getRoomOwner());
            newRoom.addRoomPlayerList(roomOwner);
            newRoom = roomRepository.save(newRoom);
            log.debug("Created Information for Room: {}", newRoom);
            return newRoom;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Something unexpected went wrong when creating a game", e);
        }
    }

    public Room findRoomById(String roomId){
        if (!roomRepository.findByRoomId(roomId).isPresent()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found");
        }
        return roomRepository.findByRoomId(roomId).get();
    }

    public void enterRoom(Room room, User user){
        if (room.getRoomPlayersList().contains(user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already in game");
        }

        // Check full or not
        if (room.getRoomPlayersList().size() >= room.getMaxPlayersNum()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This room is full!");
        }
        
        // transfer user to player
        Player player = new Player(user);
        room.addRoomPlayerList(player);
        roomRepository.save(room);
    }


    public void startGame(Room room){
        gameService.startGame(room,"0");
    }


    public List<String> getWordsRelatedTo(String query) throws IOException {

        return null;
    }


    public void checkIfGameEnd(Room roomToDo){

    }

    public String assignWord(String senderName) {
        return null;
    }


    public void exitRoom(Room room, User user){
    }

    /**
     * This is a helper method that will check the uniqueness criteria of the
     * username and the name
     * defined in the User entity. The method will do nothing if the input is unique
     * and throw an error otherwise.
     *
     * @param userToBeCreated
     * @throws org.springframework.web.server.ResponseStatusException
     * @see User
     */

}
