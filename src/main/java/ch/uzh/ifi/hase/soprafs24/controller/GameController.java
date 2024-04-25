package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.service.*;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
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
    private GameRepository gameRepository;

    public GameController(RoomService roomService, SocketService socketService, UserService userService, PlayerService playerService, GameService gameService, SimpMessagingTemplate simpMessagingTemplate, @Qualifier("gameRepository") GameRepository gameRepository) {
        this.socketService = socketService;
        this.gameService=gameService;
        this.roomService = roomService;
        this.userService = userService;
        this.gameRepository = gameRepository;
        this.playerService = playerService;
    }

    //set ready
    @MessageMapping("/message/users/ready")
    public void ready(SimpMessageHeaderAccessor headerAccessor, @Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receipId = (String) headerAccessor.getHeader("receipt");
        String userId = payload.getMessage().getUserID();
        String roomId = payload.getMessage().getRoomID();
        gameService.Ready(userId);
        socketService.broadcastPlayerInfo(roomId,receipId);
        socketService.broadcastGameinfo(roomId, receipId);
    }

    //set unready
    @MessageMapping("/message/users/unready")
    public void unready(SimpMessageHeaderAccessor headerAccessor,@Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receipId = (String) headerAccessor.getHeader("receipt");
        String userID = payload.getMessage().getUserID();
        String roomID = payload.getMessage().getRoomID();
        gameService.UnReady(userID);
        socketService.broadcastPlayerInfo(roomID, receipId);
        socketService.broadcastGameinfo(roomID, receipId);
    }

    //enterroom
    @MessageMapping("/message/users/enterroom")
    public void enterRoom(SimpMessageHeaderAccessor headerAccessor,@Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receipID = (String) headerAccessor.getHeader("receipt");
        String roomID = (String) headerAccessor.getSessionAttributes().get("roomId");
        String userID = payload.getMessage().getUserID();
        Room room=roomService.findRoomById(roomID);
        User user=userService.findUserById(userID);
        roomService.enterRoom(room, user);
        socketService.broadcastGameinfo(roomID, receipID);
        socketService.broadcastPlayerInfo(roomID, "enterroom");
    }

    //leaveroom
    @MessageMapping("/message/users/exitroom")
    public void exitRoom(SimpMessageHeaderAccessor headerAccessor,@Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receipID = (String) headerAccessor.getHeader("receipt");
        String roomID = (String) headerAccessor.getSessionAttributes().get("roomId");
        String userID = payload.getMessage().getUserID();
        Room room=roomService.findRoomById(roomID);
        User user=userService.findUserById(userID);
        roomService.exitRoom(room, user);
        if (gameRepository.findByRoomId(roomID).isPresent()) {
            socketService.broadcastGameinfo(roomID, receipID);
            socketService.broadcastPlayerInfo(roomID, "exitroom");
        }

    }


    //startgame
    @MessageMapping("/message/games/start")
    public void startGame(SimpMessageHeaderAccessor headerAccessor,@Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receipId = (String) headerAccessor.getHeader("receipt");
        String roomID = payload.getMessage().getRoomID();
//        Game game = gameService.findGameById(roomId);
//        gameService.checkIfAllReady(game);
        Room room = roomService.findRoomById(roomID);
        gameService.checkIfAllReady(room);
        // socketService.broadcastGameinfo(roomId, receipId);
    }

    //submitAnswer
    @MessageMapping("/message/games/validate")
    public void submitAnswer(SimpMessageHeaderAccessor headerAccessor,@Payload TimestampedRequest<AnswerGuess> payload) {
        String receipId = (String) headerAccessor.getHeader("receipt");

        System.out.println(payload.getMessage());
        String userID = payload.getMessage().getUserID();
        String roomID = payload.getMessage().getRoomID();
        String guess = payload.getMessage().getGuess();
        Game game = gameService.findGameById(roomID);
        System.out.println(userID);
        System.out.println(roomID);
        Player player = playerService.findPlayerById(userID);
        gameService.validateAnswer(game, player, guess);
        Player player1 = playerService.findPlayerById(userID);
        System.out.println("第二个"+player1.getScoreDetails());
    }

    //submitAudio
    @MessageMapping("/message/games/audio/upload")
    public void uploadAudio(SimpMessageHeaderAccessor headerAccessor,@Payload TimestampedRequest<PlayerAudio> payload) {
        String receipId = (String) headerAccessor.getHeader("receipt");
        String userID = payload.getMessage().getUserID();
        System.out.println("userId: "+userID);
        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");
        System.out.println("roomId: "+roomId);
        String voice = payload.getMessage().getAudioData();
        gameService.setPlayerAudio(roomId,userID,voice);
    }

    // //notify game info
    // @MessageMapping("/message/games/info")
    // public void notifyGameinfo(SimpMessageHeaderAccessor headerAccessor) {
    //     String roomID = (String) headerAccessor.getSessionAttributes().get("roomId");
    //     socketService.broadcastGameinfo(roomID, "0");
    // }

    //notify player words
//    @MessageMapping("/message/games/words")
//    public void notifyPlayerWords(SimpMessageHeaderAccessor headerAccessor) {
//        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");
//        gameService.assignWords(roomId);
//    }


    //broadcast Audio
//    @MessageMapping("/message/games/audio/notifySpeaker")
//    public void notifySpeakerAudio(SimpMessageHeaderAccessor headerAccessor) {
//        //从session中获取roomId
//        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");
//        Player currentSpeaker = gameService.getCurrentSpeaker(roomId);
//        String voice = gameService.getPlayerAudio(roomId,currentSpeaker.getId());
//        // String userId = currentSpeaker.getId();
//
//        String userId = "1";
//        socketService.broadcastSpeakerAudio(userId,roomId,voice);
//    }

//     //broadcast other player Audio
//    @MessageMapping("/message/games/audio/notifyOther")
//    public void notifyPlayerAudio(SimpMessageHeaderAccessor headerAccessor) {
//        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");
//        Map<String, String> voice = gameService.getAllPlayerAudio(roomId);
//        socketService.broadcastAudio(roomId,voice);
//    }


    // @MessageMapping("/message/response")
    // public void response(@Payload String payload) {
    //     System.out.println(payload);
    // }

    //notifyLobbyinfo
    // @MessageMapping("/message/lobby/info")
    // public void notifyLobbyinfo(@Payload Timestamped<RoomInfo> payload) {
    //     Message lobbymessage = new Message();
    //     lobbymessage.setSenderName("system");
    //     lobbymessage.setTimestamp(LocalDateTime.now());
    //     // lobbymessage.setMessageType(MessageOrderType.LOBBY);
    // }

}
