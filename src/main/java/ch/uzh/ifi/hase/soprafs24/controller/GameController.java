package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.constant.RoomProperty;
import ch.uzh.ifi.hase.soprafs24.constant.RoundStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
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
    private PlayerRepository playerRepository;

    public GameController(RoomService roomService, SocketService socketService, UserService userService, PlayerService playerService, GameService gameService, SimpMessagingTemplate simpMessagingTemplate, @Qualifier("gameRepository") GameRepository gameRepository, @Qualifier("roomRepository") RoomRepository roomRepository, @Qualifier("playerRepository") PlayerRepository playerRepository){
        this.socketService = socketService;
        this.gameService=gameService;
        this.roomService = roomService;
        this.userService = userService;
        this.gameRepository = gameRepository;
        this.playerService = playerService;
        this.roomRepository = roomRepository;
        this.playerRepository = playerRepository;
    }

    //set ready
    @MessageMapping("/message/users/ready/{roomId}")
    public void ready(SimpMessageHeaderAccessor headerAccessor,@DestinationVariable("roomId") String roomId, @Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receipId = (String) headerAccessor.getHeader("receipt");
        String userId = payload.getMessage().getUserID();
        // String roomId = payload.getMessage().getRoomID();
        gameService.Ready(userId);
        socketService.broadcastPlayerInfo(roomId,receipId);
        socketService.broadcastGameinfo(roomId, receipId);
        socketService.broadcastLobbyInfo();
    }

    //set unready
    @MessageMapping("/message/users/unready/{roomId}")
    public void unready(SimpMessageHeaderAccessor headerAccessor,@DestinationVariable("roomId") String roomId,@Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receipId = (String) headerAccessor.getHeader("receipt");
        String userID = payload.getMessage().getUserID();
        // String roomID = payload.getMessage().getRoomID();
        gameService.UnReady(userID);
        socketService.broadcastPlayerInfo(roomId, receipId);
        socketService.broadcastGameinfo(roomId, receipId);
        socketService.broadcastLobbyInfo();
    }

    //enterroom
    @MessageMapping("/message/users/enterroom/{roomId}")
    public void enterRoom(SimpMessageHeaderAccessor headerAccessor, @DestinationVariable("roomId") String roomId, @Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receipID = (String) headerAccessor.getHeader("receipt");
        // String roomID = (String) headerAccessor.getSessionAttributes().get("roomId");
        // String roomID = payload.getMessage().getRoomID();
        System.out.println("[enterRoom msg received]roomID: "+roomId);
        String userID = payload.getMessage().getUserID();
        Room room=roomService.findRoomById(userID,roomId);
        User user=userService.findUserById(userID);
        if (room.getRoomPlayersList().contains(user.getId())){
            if (room.getRoomProperty().equals(RoomProperty.INGAME)) {
                Game game = gameRepository.findByRoomId(room.getRoomId()).get();
                if (game.getRoundStatus().equals(RoundStatus.guess)){
                    String voice = playerRepository.findById(game.getCurrentSpeaker().getId()).get().getAudioData();
                    socketService.broadcastGameinfo(roomId, receipID);
                    socketService.broadcastPlayerInfo(roomId, "enterroom");
                    socketService.broadcastLobbyInfo();
                    socketService.broadcastSpeakerAudio(game.getRoomId(), game.getCurrentSpeaker().getId(),voice);
                }
            }
        }
        else {roomService.enterRoom(room, user);}
        socketService.broadcastGameinfo(roomId, receipID);
        socketService.broadcastPlayerInfo(roomId, "enterroom");
        socketService.broadcastLobbyInfo();
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
            socketService.broadcastLobbyInfo();
            socketService.broadcastPlayerInfo(roomId, "exitroom");
        }
        
        if (gameRepository.findByRoomId(roomId).isPresent()) {
            socketService.broadcastGameinfo(roomId, receipID);
            socketService.broadcastPlayerInfo(roomId, "exitroom");
            socketService.broadcastLobbyInfo();
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
        socketService.broadcastLobbyInfo();
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
    
    @MessageMapping("/message/response")
    public void response(@Payload String payload) {
        System.out.println(payload);
    }

    //notifyLobbyinfo
    @MessageMapping("/message/lobby/info")
    public void notifyLobbyInfo(SimpMessageHeaderAccessor headerAccessor) {
        // String receipId = (String) headerAccessor.getHeader("receipt");
        System.out.println("receive the lobby request!");
        socketService.broadcastLobbyInfo();
    }

}
