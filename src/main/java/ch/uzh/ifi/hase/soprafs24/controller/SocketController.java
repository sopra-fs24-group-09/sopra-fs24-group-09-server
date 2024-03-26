package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.service.RoomService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import ch.uzh.ifi.hase.soprafs24.service.SocketService;
import ch.uzh.ifi.hase.soprafs24.model.Message;

@Controller
public class SocketController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public SocketController(SocketService chatService, RoomService roomService, UserService userService, SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/message/{roomId}")
    @SendTo("/chatroom/{roomId}/public")
    public Message receiveMessage() {
        return null;
    }

    @MessageMapping("/roomcreat")
    @SendTo("/room")
    public Message receiveCreationMessage(@Payload Message message) {
        return message;
    }


    @MessageMapping("/lobbyupdate")
    @SendTo("/room")
    public Message receiveLobbyMessage(@Payload Message message) {
        simpMessagingTemplate.convertAndSend("/room", message);
        return message;
    }

    @MessageMapping( "/gamestart/{roomId}")
    public void startGame() {
    }


}
