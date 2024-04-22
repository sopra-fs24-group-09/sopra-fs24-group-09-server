package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class RoomController {


    private final RoomService roomService;

    RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    //This method is used to get all rooms in the lobby
    @GetMapping("/games/lobby")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<RoomGetDTO> getAllRooms() {
        // fetch all rooms in the internal representation，
        List<Room> rooms = roomService.getRooms();
        List<RoomGetDTO> roomGetDTOs = new ArrayList<>();

        // convert each user to the API representation
        for (Room room : rooms) {
            roomGetDTOs.add(DTOMapper.INSTANCE.convertEntityToRoomGetDTO(room));
        }
        return roomGetDTOs;
    }

    //This method is used to create a new room
    @PostMapping("/games")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public RoomGetDTO createRoom(@RequestBody RoomPostDTO roomPostDTO) {
        Room roomInput = DTOMapper.INSTANCE.convertRoomPostDTOtoEntity(roomPostDTO);
        // create room
        System.out.println(roomInput.getRoomOwnerId());
        Room createdRoom = roomService.createRoom(roomInput);
        // convert internal representation of room back to API
        return DTOMapper.INSTANCE.convertEntityToRoomGetDTO(createdRoom);
    }

    @PutMapping("/games{roomId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void enterRoom(@PathVariable String roomId,@RequestBody UserPutDTO userPutDTO) {
        User userInput = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userPutDTO);
        Room enteredRoom = roomService.findRoomById(roomId);
        roomService.enterRoom(enteredRoom, userInput);
    }

    @PostMapping("/games/guard")
    @ResponseStatus(HttpStatus.OK)
    public RoomGetDTO playerGuard(@RequestBody UserPostDTO userPostDTO) {
        return null;
    }


}
