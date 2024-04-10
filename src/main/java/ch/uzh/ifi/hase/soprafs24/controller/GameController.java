package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.RoomService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import ch.uzh.ifi.hase.soprafs24.service.SocketService;
import ch.uzh.ifi.hase.soprafs24.model.Message;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.entity.User;
@Controller
public class GameController {

    private SocketService socketService;
    private GameService gameService;
    private RoomService roomService;
    private UserService userService;

    public GameController(SocketService socketService, UserService userService, GameService gameService, SimpMessagingTemplate simpMessagingTemplate) {
        this.socketService = socketService;
        this.gameService=gameService;
    }

    //set ready
    @MessageMapping("/message/{userId}/{roomId}/ready")
    @SendTo("/room/{roomId}/public")
    public void receiveReadyMessage(@Payload Message message,@DestinationVariable("timestamp") String time,@DestinationVariable("userId") Long userId,@DestinationVariable("roomId") Long roomId) {
        gameService.Ready(userId);
        socketService.broadcastReady(roomId, true);
    }

    //set unready
    @MessageMapping("/message/{userId}/{roomId}/unready")
    public void receiveUnreadyMessage(@Payload Message message,@DestinationVariable("timestamp") String time,@DestinationVariable("userId") Long userId,@DestinationVariable("roomId") Long roomId) {
        gameService.UnReady(userId);
        socketService.broadcastReady(roomId, false);
    }


    //enterroom
    @MessageMapping("/message/{userId}/{roomId}/enterroom")
    public void enterRoom(@Payload Message message,@DestinationVariable("timestamp") String time,@DestinationVariable("userId") String userId,@DestinationVariable("roomId") String roomId) {
        Room room=roomService.findRoomById(roomId);
        User user=userService.findUserById(userId);
        roomService.enterRoom(room, user);
        socketService.broadcastEnterroom(roomId, userId);
    }

    //leaveroom
    @MessageMapping("/message/{userId}/{roomId}/exitroom")
    public void exitRoom(@Payload Message message,@DestinationVariable("timestamp") String time,@DestinationVariable("userId") String userId,@DestinationVariable("roomId") String roomId) {
        Room room=roomService.findRoomById(roomId);
        User user=userService.findUserById(userId);
        roomService.exitRoom(room, user);
        socketService.broadcastExitroom(roomId, userId);
    }


    //startgame
    @MessageMapping("/message/{userId}/{roomId}/startgame")
    public void startGame(@Payload Message message,@DestinationVariable("timestamp") String time,@DestinationVariable("userId") String userId,@DestinationVariable("roomId") String roomId) {
        Room room=roomService.findRoomById(roomId);
        roomService.startGame(room);
        socketService.broadcastGamestart(roomId);
    }


    //notifyRoominfo
    @MessageMapping("/message/{roomId}/roominfo")
    public void notifyRoominfo(@Payload Message message,@DestinationVariable("timestamp") String time,@DestinationVariable("userId") String userId,@DestinationVariable("roomId") String roomId) {
        socketService.broadcastRoominfo(roomId);
    }



}
