package ch.uzh.ifi.hase.soprafs24.service;
import ch.uzh.ifi.hase.soprafs24.constant.RoomProperty;
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
        newRoom.setRoomProperty(RoomProperty.WAITING);
//        newRoom.setRoomName(newRoom.getRoomName());
//        newRoom.setRoomOwnerId(newRoom.getRoomOwnerId());
//        newRoom.setTheme(newRoom.getTheme());
        newRoom.addRoomPlayerList(newRoom.getRoomOwnerId());
        newRoom = roomRepository.save(newRoom);
        roomRepository.save(newRoom);
        log.debug("Created Information for Room: {}", newRoom);
        return newRoom;
    }

    public Room findRoomById(String roomId){
        return roomRepository.findByRoomId(roomId).get();
    }

    public void enterRoom(Room room, User user){
        if (room.getRoomPlayersList().size()<room.getMaxPlayersNum()){
            room.addRoomPlayerList(user.getId());
        }
        else throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This room is full!");
    }


    public void startGame(Room room){
        gameService.startGame(room);
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
