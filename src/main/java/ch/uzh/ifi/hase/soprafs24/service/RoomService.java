package ch.uzh.ifi.hase.soprafs24.service;
import ch.uzh.ifi.hase.soprafs24.constant.PlayerStatus;
import ch.uzh.ifi.hase.soprafs24.constant.RoomProperty;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
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
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public RoomService(@Qualifier("roomRepository") RoomRepository roomRepository, @Qualifier("userRepository") UserRepository userRepository, @Qualifier("gameRepository") GameRepository gameRepository) {
        this.gameRepository = gameRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    public List<Room> getRooms() {
        return this.roomRepository.findAll();
    }

    public boolean checkRoomExists(String roomId){
        return roomRepository.findByRoomId(roomId).isPresent();
    }

    //Here we create a new room, and we need to set the room property and theme according to the input from client
    public Room createRoom(Room newRoom){
        Optional<Room> existingRoom = roomRepository.findByRoomId(newRoom.getRoomId());
        if (existingRoom.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room already exists");
        }

        //check if the room name contains spaces or special characters
        if (newRoom.getRoomName().matches(".*[\\s\\p{Punct}].*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room name should not contain spaces or special characters");
        }
        
        //check if the number of players is between 2 and 5
        if (newRoom.getMaxPlayersNum() < 2 || newRoom.getMaxPlayersNum() > 5){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The number of players should be between 2 and 5");
        }

        try {
            newRoom.setRoomName(newRoom.getRoomName());
            newRoom.setTheme(newRoom.getTheme());
            newRoom.setMaxPlayersNum(newRoom.getMaxPlayersNum());
            newRoom.setRoomOwnerId(newRoom.getRoomOwnerId());
            newRoom.setRoomProperty(RoomProperty.WAITING);
            newRoom.addRoomPlayerList(newRoom.getRoomOwnerId());
//            newRoom.setRoomPlayersList(newRoom.getRoomPlayersList());

            newRoom = roomRepository.save(newRoom);

            User roomOwner = userRepository.findById(newRoom.getRoomOwnerId()).get();
            roomOwner.setPlayerStatus(PlayerStatus.READY);
            roomOwner.setInRoomId(newRoom.getRoomId());
            userRepository.save(roomOwner);
            log.debug("Created Information for Room: {}", newRoom);
            return newRoom;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Something unexpected went wrong when creating a game", e);
        }
    }

    // public Room findRoomById(String userId, String roomId){
    //     if (!roomRepository.findByRoomId(roomId).isPresent()){
    //         // String jsonMessage = "{\"message\":\"Room not found!\"}"; 
    //         // template.convertAndSendToUser(userId, "/response/"+ roomId, jsonMessage);
    //         throw new RuntimeException( "Room not found");
    //     }
    //     return roomRepository.findByRoomId(roomId).get();
    // }

    public <T> void enterRoom(Room room, User user){
        // if (room.getRoomPlayersList().contains(user.getId())) {
        //     throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already in game");
        // }
        // Check full or not
        if (room.getRoomPlayersList().size() >= room.getMaxPlayersNum()) {
            // String jsonMessage = "{\"message\":\"This room is full!\"}"; 
            // template.convertAndSendToUser(user.getId(), "/response/"+ room.getRoomId(), jsonMessage);
            throw new RuntimeException( "This room is full!");
        }
        if (room.getRoomProperty() != RoomProperty.WAITING){
            // String jsonMessage = "{\"message\":\"You can not enter a room that is in game!\"}"; 
            // template.convertAndSendToUser(user.getId(), "/response/"+ room.getRoomId(), jsonMessage);
            throw new RuntimeException("You can not enter a room that is in game!");
        }

        if (user.getInRoomId()!=null && !user.getInRoomId().equals(room.getRoomId())){
            // String jsonMessage = "{\"message\":\"You can not enter a room when you are in another room!\"}";
            // template.convertAndSendToUser(user.getId(), "/response/"+ room.getRoomId(), jsonMessage);
            throw new RuntimeException("You can not enter a room when you are in another room!");
        }
        boolean id_equal = (user.getId()).equals(room.getRoomOwnerId());

        //if the user is not room owner then set the status to unready
        if (!id_equal){
            user.setPlayerStatus(PlayerStatus.UNREADY);
            user.setInRoomId(room.getRoomId());
        }
        room.addRoomPlayerList(user.getId());
        System.out.println("Roomplayerslist now is:"+room.getRoomPlayersList());
        userRepository.save(user);
        roomRepository.save(room);
    }


    public void exitRoom(Room room, User user){
        if (!room.getRoomPlayersList().contains(user.getId())) {
            throw new RuntimeException( "User is not in game");
        }
        if (room.getRoomPlayersList().size() == 1){
            if (gameRepository.findByRoomId(room.getRoomId()).isPresent()){
                gameRepository.delete(gameRepository.findByRoomId(room.getRoomId()).get());
            }
            roomRepository.delete(room);
        }
        else{
            if(room.getRoomOwnerId().equals(user.getId()) && room.getRoomPlayersList().size() > 1) {
                room.setRoomOwnerId(room.getRoomPlayersList().get(1));
                User newOwner = userRepository.findById(room.getRoomPlayersList().get(1)).get();
                newOwner.setPlayerStatus(PlayerStatus.READY);
                userRepository.save(newOwner);
            }
            room.getRoomPlayersList().remove(user.getId());
            roomRepository.save(room);
        }
        user.setPlayerStatus(PlayerStatus.UNREADY);
        user.setInRoomId(null);
        userRepository.save(user);
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
