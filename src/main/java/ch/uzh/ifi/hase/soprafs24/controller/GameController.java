package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.service.*;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import ch.uzh.ifi.hase.soprafs24.model.AnswerGuess;
import ch.uzh.ifi.hase.soprafs24.model.PlayerAndRoom;
import ch.uzh.ifi.hase.soprafs24.model.PlayerAudio;
import ch.uzh.ifi.hase.soprafs24.model.TimestampedRequest;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.entity.User;
@Controller
public class GameController {

    private SocketService socketService;
    private GameService gameService;
    private RoomService roomService;
    private UserService userService;
    private PlayerService playerService;

    public GameController(RoomService roomService, SocketService socketService, UserService userService, PlayerService playerService, GameService gameService, SimpMessagingTemplate simpMessagingTemplate) {
        this.socketService = socketService;
        this.gameService=gameService;
        this.roomService = roomService;
        this.userService = userService;
    }

    //set ready
    @MessageMapping("/message/users/ready")
    public void ready(SimpMessageHeaderAccessor headerAccessor, @Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receipId = (String) headerAccessor.getHeader("receipt");
        String userId = payload.getMessage().getUserID();
        String roomId = payload.getMessage().getRoomID();
        // gameService.Ready(userId);
        socketService.broadcastPlayerInfo(roomId, userId,receipId);
        socketService.broadcastGameinfo(roomId, receipId);
    }

    //set unready
    @MessageMapping("/message/users/unready")
    public void unready(SimpMessageHeaderAccessor headerAccessor,@Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receipId = (String) headerAccessor.getHeader("receipt");
        String userId = payload.getMessage().getUserID();
        String roomId = payload.getMessage().getRoomID();
        gameService.UnReady(userId);
        socketService.broadcastPlayerInfo(roomId, userId,receipId);
        socketService.broadcastGameinfo(roomId, receipId);
    }

    //enterroom
    @MessageMapping("/message/games/enterroom")
    public void enterRoom(SimpMessageHeaderAccessor headerAccessor,@Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receipId = (String) headerAccessor.getHeader("receipt");
        String roomId = payload.getMessage().getRoomID();
        // Room room=roomService.findRoomById(roomId);
        // User user=userService.findUserById(userId);

        //测使用
        Room room = new Room();
        room.setRoomId(roomId);
        room.setMaxPlayersNum(4);
        User user = new User();

        roomService.enterRoom(room, user);
        socketService.broadcastGameinfo(roomId, receipId);
    }

    //leaveroom
    @MessageMapping("/message/games/exitRoom")
    public void exitRoom(SimpMessageHeaderAccessor headerAccessor,@Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receipId = (String) headerAccessor.getHeader("receipt");
        String userId = payload.getMessage().getUserID();
        String roomId = payload.getMessage().getRoomID();
        Room room= roomService.findRoomById(roomId);
        User user= userService.findUserById(userId);
        socketService.broadcastGameinfo(roomId, receipId);
        roomService.exitRoom(room, user);

    }


    //startgame
    @MessageMapping("/message/games/start")
    public void startGame(SimpMessageHeaderAccessor headerAccessor,@Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receipId = (String) headerAccessor.getHeader("receipt");
        String roomId = payload.getMessage().getRoomID();
//        Game game = gameService.findGameById(roomId);
//        gameService.checkIfAllReady(game);
        Room room = roomService.findRoomById(roomId);
        gameService.checkIfAllReady(room);
        socketService.broadcastGameinfo(roomId, receipId);
    }

    //submitAnswer
    @MessageMapping("/message/games/validate")
    public void submitAnswer(SimpMessageHeaderAccessor headerAccessor,@Payload TimestampedRequest<AnswerGuess> payload) {
        String receipId = (String) headerAccessor.getHeader("receipt");
        String userId = payload.getMessage().getUseId();
        String roomId = payload.getMessage().getRoomId();
        String guess = payload.getMessage().getGuess();
        Long roundNum = payload.getMessage().getRoundNum();
        String currentSpeakerId = payload.getMessage().getCurrentSpeakerId();
        Game game = gameService.findGameById(roomId);
        Player player = playerService.findPlayerById(userId);
        gameService.validateAnswer(game, player, guess);
    }

    //submitAudio
    @MessageMapping("/message/games/Audio/upload")
    public void uploadAudio(SimpMessageHeaderAccessor headerAccessor,@Payload TimestampedRequest<PlayerAudio> payload) {
        String receipId = (String) headerAccessor.getHeader("receipt");
        String userId = payload.getMessage().getUserId();
        String roomId = payload.getMessage().getRoomId();
        String voice = payload.getMessage().getAudioData();
        gameService.setPlayerAudio(roomId,userId,voice);
    }

    //notify game info
    @MessageMapping("/message/games/info")
    public void notifyGameinfo(SimpMessageHeaderAccessor headerAccessor) {
        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");
        socketService.broadcastGameinfo(roomId, "0");
    }

    //notify player words
//    @MessageMapping("/message/games/words")
//    public void notifyPlayerWords(SimpMessageHeaderAccessor headerAccessor) {
//        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");
//        gameService.assignWords(roomId);
//    }


    //broadcast Audio
    @MessageMapping("/message/games/Audio/notifySpeaker")
    public void notifySpeakerAudio(SimpMessageHeaderAccessor headerAccessor) {
        //从session中获取roomId
        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");
        Player currentSpeaker = gameService.getCurrentSpeaker(roomId);
        String voice = gameService.getPlayerAudio(roomId,currentSpeaker.getId());
        // String userId = currentSpeaker.getId();

        String userId = "1";
        socketService.broadcastSpeakerAudio(userId,roomId,voice);
    }

    //broadcast other player Audio
   @MessageMapping("/message/games/Audio/notifyOther")
   public void notifyPlayerAudio(SimpMessageHeaderAccessor headerAccessor) {
       String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");
       Map<String, String> voice = gameService.getAllPlayerAudio(roomId);
       socketService.broadcastAudio(roomId,voice);
   }


    @MessageMapping("/message/response")
    public void response(@Payload String payload) {
        System.out.println(payload);
    }

    //notifyLobbyinfo
    // @MessageMapping("/message/lobby/info")
    // public void notifyLobbyinfo(@Payload Timestamped<RoomInfo> payload) {
    //     Message lobbymessage = new Message();
    //     lobbymessage.setSenderName("system");
    //     lobbymessage.setTimestamp(LocalDateTime.now());
    //     // lobbymessage.setMessageType(MessageOrderType.LOBBY);
    // }

}
