package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class RoomController {


    RoomController(RoomService roomService) {
    }

    @GetMapping("/games")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<RoomGetDTO> getAllRooms() {
        // fetch all rooms in the internal representation，
        List<RoomGetDTO> roomGetDTOs = new ArrayList<>();
        return roomGetDTOs;
    }

    @PostMapping("/games/room")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public RoomGetDTO createRoom(@RequestBody RoomPostDTO roomPostDTO) {
        // convert API room to internal representation
        // convert internal representation of room back to API
        return null;
    }

    //Get method for getting one room
    @GetMapping("/games/{roomId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public RoomGetDTO roomInfo (@PathVariable("roomId") Long roomId) {
        return null;
    }


    @PutMapping("/room/{roomId}/players")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void enterRoom(@PathVariable("roomId") Long roomId, @RequestBody UserPutDTO userPutDTO) {
    }

    @PutMapping("/room/{roomId}/vote/{voterId}={voteeId}")
    @ResponseStatus(HttpStatus.OK)
    public void castVote(@PathVariable Long roomId, @PathVariable Long voterId, @PathVariable Long voteeId) {
    }

    @PostMapping("/games/guard")
    @ResponseStatus(HttpStatus.OK)
    public RoomGetDTO playerGuard(@RequestBody UserPostDTO userPostDTO) {
        return null;
    }


}
