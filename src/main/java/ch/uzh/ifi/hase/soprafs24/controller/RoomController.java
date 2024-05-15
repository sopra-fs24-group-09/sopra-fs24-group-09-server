package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.annotation.UserLoginToken;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.service.RoomService;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private final RoomRepository roomRepository;

    RoomController(RoomService roomService, @Qualifier("roomRepository") RoomRepository roomRepository) {
        this.roomService = roomService;
        this.roomRepository = roomRepository;
    }

    //This method is used to get all rooms in the lobby
    @UserLoginToken
    @GetMapping("/games/lobby")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<RoomGetDTO> getAllRooms() {
        // fetch all rooms in the internal representation，
        List<Room> rooms = roomService.getRooms();
        List<RoomGetDTO> roomGetDTOs = new ArrayList<>();

        // convert each user to the API representation
        for (Room room : rooms) {
            if (room.getRoomPlayersList().size() == 0) {
                roomRepository.delete(room);
            }
            else {
                roomGetDTOs.add(DTOMapper.INSTANCE.convertEntityToRoomGetDTO(room));
            }
        }
        return roomGetDTOs;
    }

    //This method is used to create a new room
    @UserLoginToken
    @PostMapping("/games")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public RoomGetDTO createRoom(@RequestBody RoomPostDTO roomPostDTO) {
        Room roomInput = DTOMapper.INSTANCE.convertRoomPostDTOtoEntity(roomPostDTO);
        // create room
        Room createdRoom = roomService.createRoom(roomInput);
        // convert internal representation of room back to API
        return DTOMapper.INSTANCE.convertEntityToRoomGetDTO(createdRoom);
    }


    @PostMapping("/games/guard")
    @ResponseStatus(HttpStatus.OK)
    public RoomGetDTO playerGuard(@RequestBody UserPostDTO userPostDTO) {
        return null;
    }



}
