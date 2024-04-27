package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs24.service.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.DestinationVariable;
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
    private RoomRepository roomRepository;

    public GameController(RoomService roomService, SocketService socketService, UserService userService, PlayerService playerService, GameService gameService, SimpMessagingTemplate simpMessagingTemplate, @Qualifier("gameRepository") GameRepository gameRepository, @Qualifier("roomRepository") RoomRepository roomRepository){
        this.socketService = socketService;
        this.gameService=gameService;
        this.roomService = roomService;
        this.userService = userService;
        this.gameRepository = gameRepository;
        this.playerService = playerService;
        this.roomRepository = roomRepository;
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
    @MessageMapping("/message/users/enterroom/{roomId}")
    public void enterRoom(SimpMessageHeaderAccessor headerAccessor, @DestinationVariable("roomId") String roomId, @Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receipID = (String) headerAccessor.getHeader("receipt");
        String roomID = (String) headerAccessor.getSessionAttributes().get("roomId");
        String userID = payload.getMessage().getUserID();
        Room room=roomService.findRoomById(userID,roomID);
        User user=userService.findUserById(userID);
        roomService.enterRoom(room, user);
        socketService.broadcastGameinfo(roomID, receipID);
        socketService.broadcastPlayerInfo(roomID, "enterroom");
    }

    //leaveroom
    @MessageMapping("/message/users/exitroom/{roomId}")
    public void exitRoom(SimpMessageHeaderAccessor headerAccessor,@DestinationVariable("roomId") String roomId,@Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receipID = (String) headerAccessor.getHeader("receipt");
        String userID = payload.getMessage().getUserID();

        User user=userService.findUserById(userID);
        if(roomRepository.findByRoomId(roomId).isPresent()){
            Room room=roomService.findRoomById(userID,roomId);
            roomService.exitRoom(room, user);
        }
        
        if (gameRepository.findByRoomId(roomId).isPresent()) {
            socketService.broadcastGameinfo(roomId, receipID);
            socketService.broadcastPlayerInfo(roomId, "exitroom");
        }

    }


    //startgame
    @MessageMapping("/message/games/start/{roomId}")
    public void startGame(SimpMessageHeaderAccessor headerAccessor,@DestinationVariable("roomId") String roomId, @Payload TimestampedRequest<PlayerAndRoom> payload) {
        // String receipId = (String) headerAccessor.getHeader("receipt");
        // String roomID = payload.getMessage().getRoomID();
        String userID = payload.getMessage().getUserID();
        Room room = roomService.findRoomById(userID,roomId);
        gameService.checkIfAllReady(room);
    }

    //submitAnswer
    @MessageMapping("/message/games/validate/{roomId}")
    public void submitAnswer(SimpMessageHeaderAccessor headerAccessor,@DestinationVariable("roomId") String roomId,@Payload TimestampedRequest<AnswerGuess> payload) {
        // String receipId = (String) headerAccessor.getHeader("receipt");
        String userID = payload.getMessage().getUserID();
        // String roomID = payload.getMessage().getRoomID();
        String guess = payload.getMessage().getGuess();
        Game game = gameService.findGameById(roomId);
        Player player = playerService.findPlayerById(userID);
        gameService.validateAnswer(game, player, guess);
    }

    //submitAudio
    @MessageMapping("/message/games/audio/upload/{roomId}")
    public void uploadAudio(SimpMessageHeaderAccessor headerAccessor,@DestinationVariable("roomId") String roomId,@Payload TimestampedRequest<PlayerAudio> payload) {
        // String receipId = (String) headerAccessor.getHeader("receipt");
        String userID = payload.getMessage().getUserID();
        String voice = payload.getMessage().getAudioData();
        gameService.setPlayerAudio(roomId,userID,voice);
    }
    
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
