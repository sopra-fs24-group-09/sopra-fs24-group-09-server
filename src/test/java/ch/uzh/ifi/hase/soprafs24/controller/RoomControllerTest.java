package ch.uzh.ifi.hase.soprafs24.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.rest.dto.RoomGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.RoomPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.RoomService;
import ch.uzh.ifi.hase.soprafs24.service.SocketService;

import static org.mockito.Mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomControllerTest {

   @Mock
   private RoomService roomService;

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
       // when
       List<RoomGetDTO> rooms = roomController.getAllRooms();

       // then
       assertNotNull(rooms);
       verify(roomService, times(1)).getRooms();
   }

   @Test
   public void testCreateRoom() {
       RoomPostDTO roomPostDTO = new RoomPostDTO();
       Room roomInput = new Room();
       Room createdRoom = new Room();
       RoomGetDTO roomGetDTO = new RoomGetDTO();

       when(DTOMapper.INSTANCE.convertRoomPostDTOtoEntity(roomPostDTO)).thenReturn(roomInput);
       when(roomService.createRoom(roomInput)).thenReturn(createdRoom);
       when(DTOMapper.INSTANCE.convertEntityToRoomGetDTO(createdRoom)).thenReturn(roomGetDTO);

       RoomGetDTO result = roomController.createRoom(roomPostDTO);

       verify(roomService).createRoom(roomInput);
       assertEquals(roomGetDTO, result);
   }

}