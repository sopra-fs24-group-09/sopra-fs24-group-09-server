package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.service.RoomService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
// import org.springframework.messaging.handler.annotation.DestinationVariable;
// import org.springframework.messaging.handler.annotation.MessageMapping;
// import org.springframework.messaging.handler.annotation.Payload;
// import org.springframework.messaging.handler.annotation.SendTo;
// import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import ch.uzh.ifi.hase.soprafs24.service.SocketService;

@Controller
public class SocketController {

    @Autowired
    @Lazy
    private SocketService chatService;

    public SocketController(RoomService roomService, UserService userService) {
    }

    // //mapping to the message sent from the room
    // @MessageMapping("/message/{roomId}")
    // @SendTo("/chatroom/{roomId}/public")
    // public Message receiveMessage() {
    //     return null;
    // }

    // @MessageMapping("/roomcreat")
    // @SendTo("/room")
    // public Message receiveCreationMessage(@Payload Message message) {
    //     return message;
    // }


    // @MessageMapping("/lobbyupdate")
    // @SendTo("/room")
    // public Message receiveLobbyMessage() {
    //     return message;
    // }

    // @MessageMapping( "/gamestart/{roomId}")
    // public void startGame() {
    // }


}
