package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.RoomService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.service.SocketService;
import ch.uzh.ifi.hase.soprafs24.model.Message;
import ch.uzh.ifi.hase.soprafs24.constant.MessageOrderType;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
@Controller
public class SocketController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final SocketService socketService;
    private final GameService gameService;

    public SocketController(SocketService socketService, RoomService roomService, UserService userService, GameService gameService, SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.socketService = socketService;
        this.gameService=gameService;
    }

    //set ready
    @MessageMapping("/message/{userId}/{roomId}/ready")
    @SendTo("/room/{roomId}/public")
    public Message receiveReadyMessage(@Payload Message message,@DestinationVariable("userId") Long userId,@DestinationVariable("roomId") Long roomId) {
        gameService.Ready(userId);
        socketService.broadcastReady(roomId, true);
        Message wordMessage = new Message();
        wordMessage.setSenderName("system");
        wordMessage.setMessageType(MessageOrderType.JOIN);
        simpMessagingTemplate.convertAndSend("/room/"+roomId+"/public",  wordMessage);
        
        return wordMessage;
    }

    //set unready
    @MessageMapping("/message/{userId}/{roomId}/unready")
    @SendTo("/room/{roomId}/public")
    public Message receiveUnreadyMessage(@Payload Message message,@DestinationVariable("userId") Long userId,@DestinationVariable("roomId") Long roomId) {
        gameService.UnReady(userId);
        socketService.broadcastReady(roomId, false);
        Message wordMessage = new Message();
        wordMessage.setSenderName("system");
        wordMessage.setMessageType(MessageOrderType.UNREADY);
        simpMessagingTemplate.convertAndSend("/room/"+roomId+"/public",  wordMessage);
        return message;
    }



}
