package ch.uzh.ifi.hase.soprafs24.service;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    public RoomService() {
    }


    //Here we create a new room and we need to set the room property and theme according to the input from client
    public Room createRoom() {
        return null;
    }

    public Room findRoomById() {
        return null;
    }

    public void enterRoom(Room room, User user){
    }


    public List<String> getWordsRelatedTo(String query) throws IOException {

        return null;
    }


    public void checkIfGameEnd(Room roomToDo){

    }

    public String assignWord(String senderName) {
        return null;
    }


    public void leaveRoom(Room room, Long userId){
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
