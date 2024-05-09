package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.annotation.UserLoginToken;
import ch.uzh.ifi.hase.soprafs24.constant.RoomProperty;
import ch.uzh.ifi.hase.soprafs24.constant.RoundStatus;
import ch.uzh.ifi.hase.soprafs24.constant.RoomProperty;
import ch.uzh.ifi.hase.soprafs24.constant.RoundStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
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
import java.util.Map;
import java.util.List;

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
        this.playerRepository = playerRepository;
    }

    //set ready
    @UserLoginToken
    @MessageMapping("/message/users/ready/{roomId}")
    public void ready(SimpMessageHeaderAccessor headerAccessor, @DestinationVariable("roomId") String roomId, @Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receiptId = null; // 初始化receiptId
        String userId = payload.getMessage().getUserID();

        // 尝试从nativeHeaders中获取receiptId
        @SuppressWarnings("unchecked")
        Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) headerAccessor.getHeader("nativeHeaders");
        if (nativeHeaders != null && nativeHeaders.containsKey("receiptId")) {
            receiptId = nativeHeaders.get("receiptId").get(0);
            System.out.println("Receipt ID found for roomId: " + roomId);
        } else {
            System.out.println("Receipt ID not found " + roomId);
        }

        try {
            gameService.Ready(userId, roomId);
            socketService.broadcastResponse(userId, roomId, true,"ready " + userId, receiptId);
            socketService.broadcastPlayerInfo(roomId, receiptId);
            socketService.broadcastGameinfo(roomId, receiptId);
            socketService.broadcastLobbyInfo();
        } catch (Exception e) {
            socketService.broadcastResponse(userId, roomId,false, "ready" + userId, receiptId);
        }
    }

    //set unready
    @UserLoginToken
    @MessageMapping("/message/users/unready/{roomId}")
    public void unready(SimpMessageHeaderAccessor headerAccessor, @DestinationVariable("roomId") String roomId, @Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receiptId = null; // Initialize receiptId
        String userId = payload.getMessage().getUserID();
    
        // Try to extract receiptId from nativeHeaders
        @SuppressWarnings("unchecked")
        Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) headerAccessor.getHeader("nativeHeaders");
        if (nativeHeaders != null && nativeHeaders.containsKey("receiptId")) {
            receiptId = nativeHeaders.get("receiptId").get(0);
            System.out.println("Receipt ID found for roomId: " + roomId);
        } else {
            System.out.println("Receipt ID not found for roomId: " + roomId);
        }
    
        try {
            gameService.UnReady(userId);
            socketService.broadcastResponse(userId, roomId, true, "unready " + userId, receiptId);
            socketService.broadcastPlayerInfo(roomId, receiptId);
            socketService.broadcastGameinfo(roomId, receiptId);
            socketService.broadcastLobbyInfo();
        } catch (Exception e) {
            socketService.broadcastResponse(userId, roomId,false, "ready" + userId, receiptId);
        }
    }


    //enterroom
    @UserLoginToken
    @MessageMapping("/message/users/enterroom/{roomId}")
    public void enterRoom(SimpMessageHeaderAccessor headerAccessor, @DestinationVariable("roomId") String roomId, @Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receiptID = null; // Initialize receiptId
        String userID = payload.getMessage().getUserID();

        // Try to extract receiptId from nativeHeaders
        @SuppressWarnings("unchecked")
        Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) headerAccessor.getHeader("nativeHeaders");
        if (nativeHeaders != null && nativeHeaders.containsKey("receiptId")) {
            receiptID = nativeHeaders.get("receiptId").get(0);
            System.out.println("Receipt ID found for roomId: " + roomId);
        } else {
            System.out.println("Receipt ID not found for roomId: " + roomId);
        }

        System.out.println("[enterRoom msg received] RoomID: " + roomId + ", UserID: " + userID);

        try {
            Room room=roomService.findRoomById(userID,roomId);
            User user = userService.findUserById(userID);
            if (room != null) {
                    //if the user is already in the room
                    if (room.getRoomPlayersList().contains(user.getId())) {
                        //if the game is started and the user is entering the room
                        if (room.getRoomProperty().equals(RoomProperty.INGAME)) {
                            Game game = gameRepository.findByRoomId(room.getRoomId()).get();
                            if (game.getRoundStatus().equals(RoundStatus.guess)) {
                                String voice = playerRepository.findById(game.getCurrentSpeaker().getId()).get().getAudioData();
                                socketService.broadcastGameinfo(roomId, receiptID);
                                socketService.broadcastPlayerInfo(roomId, "enterroom");
                                socketService.broadcastLobbyInfo();
                                socketService.broadcastSpeakerAudio(game.getRoomId(), game.getCurrentSpeaker().getId(), voice);
                            }
                        }
                        //if the game is not started and the user is entering the room
                        else {
                            socketService.broadcastGameinfo(roomId, receiptID);
                            socketService.broadcastPlayerInfo(roomId, "enterroom");
                            socketService.broadcastLobbyInfo();
                            socketService.broadcastResponse(userID, roomId, true, "enter room", receiptID);
                        }
                    }
                    //if the user is not in the room
                    else {
                        System.out.println("User " + user.getUsername() + " is entering room " + room.getRoomId());
                        roomService.enterRoom(room, user);
                        socketService.broadcastGameinfo(roomId, receiptID);
                        socketService.broadcastPlayerInfo(roomId, "enterRoom");
                        socketService.broadcastLobbyInfo();
                        socketService.broadcastResponse(userID, roomId, true, "enter room", receiptID);
                    }
                }   

        } catch (Exception e) {
            // Log error or handle exception
            System.out.println("Error entering room: " + e.getMessage());
            socketService.broadcastResponse(userID, roomId, false, e.getMessage(), receiptID);
        }
    }

    //leaveroom
    @UserLoginToken
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
    @UserLoginToken
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
    @UserLoginToken
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
    @UserLoginToken
    @MessageMapping("/message/games/audio/upload/{roomId}")
    public void uploadAudio(SimpMessageHeaderAccessor headerAccessor,@DestinationVariable("roomId") String roomId,@Payload TimestampedRequest<PlayerAudio> payload) {
        // String receipId = (String) headerAccessor.getHeader("receipt");
        String userID = payload.getMessage().getUserID();
        String voice = payload.getMessage().getAudioData();
        gameService.setPlayerAudio(roomId,userID,voice);
    }

    @UserLoginToken
    @MessageMapping("/message/response")
    public void response(@Payload String payload) {
        System.out.println(payload);
    }

    //notifyLobbyinfo
    @UserLoginToken
    @MessageMapping("/message/lobby/info")
    public void notifyLobbyInfo(SimpMessageHeaderAccessor headerAccessor) {
        // String receipId = (String) headerAccessor.getHeader("receipt");
        System.out.println("receive the lobby request!");
        socketService.broadcastLobbyInfo();
    }

}
