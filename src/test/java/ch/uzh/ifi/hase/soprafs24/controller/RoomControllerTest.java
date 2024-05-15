package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.RoomGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.RoomPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.RoomService;
import ch.uzh.ifi.hase.soprafs24.service.SocketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


@ActiveProfiles("test")
class RoomControllerTest {

    @Mock
    private RoomService roomService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private DTOMapper dtoMapper;

    @Mock
    private SocketService socketService;

    @InjectMocks
    private RoomController roomController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    void testGetAllRooms() {
        List<Room> rooms = new ArrayList<>();
        Room room1 = new Room();
        room1.setRoomId("1");
        rooms.add(room1);

        when(roomService.getRooms()).thenReturn(rooms);

        List<RoomGetDTO> roomGetDTOs = roomController.getAllRooms();

        assertNotNull(roomGetDTOs);
        verify(roomService, times(1)).getRooms();
    }

    @Test
    void testGetAllRooms_withEmptyRoom() {
        List<Room> rooms = new ArrayList<>();
        Room room1 = new Room();
        room1.setRoomId("1");
        rooms.add(room1);

        when(roomService.getRooms()).thenReturn(rooms);

        List<RoomGetDTO> roomGetDTOs = roomController.getAllRooms();

        assertNotNull(roomGetDTOs);
        verify(roomService, times(1)).getRooms();
        verify(roomRepository, times(1)).delete(any(Room.class));
    }

    @Test
    public void testCreateRoom() {
        RoomPostDTO roomPostDTO = new RoomPostDTO();
        Room roomInput = new Room();
        roomInput.setRoomId("1");
        Room createdRoom = new Room();
        RoomGetDTO roomGetDTO = new RoomGetDTO();

        when(dtoMapper.convertRoomPostDTOtoEntity(roomPostDTO)).thenReturn(roomInput);
        when(roomService.createRoom(roomInput)).thenReturn(createdRoom);
        when(dtoMapper.convertEntityToRoomGetDTO(createdRoom)).thenReturn(roomGetDTO);
        roomController.createRoom(roomPostDTO);
        verify(roomService).createRoom(any(Room.class));
    }

    @Test
    void testPlayerGuard() {
        UserPostDTO userPostDTO = new UserPostDTO();

        RoomGetDTO result = roomController.playerGuard(userPostDTO);

        assertNull(result);
    }

}