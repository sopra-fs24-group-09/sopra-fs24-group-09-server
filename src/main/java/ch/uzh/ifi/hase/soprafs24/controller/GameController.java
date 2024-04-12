package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.service.*;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import ch.uzh.ifi.hase.soprafs24.model.Message;
import ch.uzh.ifi.hase.soprafs24.constant.MessageOrderType;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.entity.User;
@Controller
public class GameController {

    private SocketService socketService;
    private GameService gameService;
    private RoomService roomService;
    private UserService userService;
    private PlayerService playerService;

    public GameController(SocketService socketService, UserService userService, PlayerService playerService, GameService gameService, SimpMessagingTemplate simpMessagingTemplate) {
        this.socketService = socketService;
        this.gameService=gameService;
    }

    //set ready
    @MessageMapping("/message/{userId}/{roomId}/ready")
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
    @MessageMapping("/message/{roomId}/startgame")
    public void startGame(@Payload Message message,@DestinationVariable("timestamp") String time,@DestinationVariable("roomId") String roomId) {
        Room room=roomService.findRoomById(roomId);
        roomService.startGame(room);
        socketService.broadcastGamestart(roomId);
    }


    //notifyRoominfo
    @MessageMapping("/message/{roomId}/roominfo")
    public void notifyRoominfo(@Payload Message message,@DestinationVariable("timestamp") String time,@DestinationVariable("roomId") String roomId) {
        socketService.broadcastRoominfo(roomId);
    }

    //notifyLobbyinfo
    @MessageMapping("/message/lobbyinfo")
    @SendTo("/lobby")
    public Message notifyLobbyinfo(@Payload Message message,@DestinationVariable("timestamp") String time) {
        Message lobbymessage = new Message();
        lobbymessage.setSenderName("system");
        lobbymessage.setTimestamp(LocalDateTime.now());
        lobbymessage.setMessageType(MessageOrderType.LOBBY);
        return lobbymessage;
    }

    //notify game info
    @MessageMapping("/message/{roomId}/gameinfo")
    public void notifyGameinfo(@Payload Message message,@DestinationVariable("timestamp") String time,@DestinationVariable("userId") String userId,@DestinationVariable("roomId") String roomId) {
        socketService.broadcastGameinfo(roomId);
    }

    //notify player words
    @MessageMapping("/message/{userId}/{roomId}/validate")
    public void notifyPlayerWords(@Payload Message message,@DestinationVariable("timestamp") String time,@DestinationVariable("userId") String userId,@DestinationVariable("roomId") String roomId) {
        socketService.broadcastPlayerwords(roomId, userId);
    }

    //submitAnswer
    @MessageMapping("/message/{userId}/{roomId}/validate")
    public void submitAnswer(@Payload Message message,@DestinationVariable("timestamp") String time,@DestinationVariable("userId") String userId,@DestinationVariable("roomId") String roomId) {
        String answer = message.getMessage();
        Game game = gameService.findGameById(roomId);
        Player player = playerService.findPlayerById(userId);
        gameService.validateAnswer(game, player, answer);
    }

    //submitAudio
    @MessageMapping("/message/{userId}/{roomId}/audio/upload")
    public void uploadAudio(@Payload Message message,@DestinationVariable("timestamp") String time,@DestinationVariable("userId") String userId,@DestinationVariable("roomId") String roomId) {
        String voice = message.getMessage();
        gameService.setPlayerAudio(roomId,userId,voice);
    }

    //broadcast Audio
    @MessageMapping("/message/{roomId}/audio/download/{userId}")
    public void notifySpeakerAudio(@Payload Message message,@DestinationVariable("timestamp") String time,@DestinationVariable("userId") String userId,@DestinationVariable("roomId") String roomId) {
        String voice = gameService.getPlayerAudio(roomId,userId);
        socketService.broadcastSpeakerAudio(roomId,userId,voice);
    }

    //broadcast other player Audio
    @MessageMapping("/message/{roomId}/audio/download/all")
    public void notifyPlayerAudio(@Payload Message message,@DestinationVariable("timestamp") String time,@DestinationVariable("userId") String userId,@DestinationVariable("roomId") String roomId) {
        Map<String, String> voice = gameService.getAllPlayerAudio(roomId);
        socketService.broadcastAudio(roomId,voice);
    }

}
