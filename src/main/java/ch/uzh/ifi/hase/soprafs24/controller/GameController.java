package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.constant.RoomProperty;
import ch.uzh.ifi.hase.soprafs24.constant.RoundStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
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
    private PlayerRepository playerRepository;

    public GameController(RoomService roomService, SocketService socketService, UserService userService, PlayerService playerService, GameService gameService, SimpMessagingTemplate simpMessagingTemplate, @Qualifier("gameRepository") GameRepository gameRepository, @Qualifier("playerRepository") PlayerRepository playerRepository){
        this.socketService = socketService;
        this.gameService=gameService;
        this.roomService = roomService;
        this.userService = userService;
        this.gameRepository = gameRepository;
        this.playerService = playerService;
        this.playerRepository = playerRepository;
        this.playerRepository = playerRepository;
    }

    //set ready
    @MessageMapping("/message/users/ready/{roomId}")
    public void ready(SimpMessageHeaderAccessor headerAccessor, @DestinationVariable("roomId") String roomId, @Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receiptId = null; // Initialize receiptId
        String userID = payload.getMessage().getUserID();

        // Try to extract receiptId from nativeHeaders
        @SuppressWarnings("unchecked")
        Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) headerAccessor.getHeader("nativeHeaders");
        if (nativeHeaders != null && nativeHeaders.containsKey("receiptId")) {
            receiptId = nativeHeaders.get("receiptId").get(0);
            System.out.println("Receipt ID found for roomId: " + roomId);
        } else {
            System.out.println("Receipt ID not found " + roomId);
        }

        System.out.println("[ready msg received] RoomID: " + roomId + ", UserID: " + userID);

        try {
            gameService.Ready(userID, roomId);
            socketService.broadcastResponse(userID, roomId, true,"ready " + userID, receiptId);
            socketService.broadcastPlayerInfo(roomId, receiptId);
            socketService.broadcastGameinfo(roomId, receiptId);
            // socketService.broadcastLobbyInfo();
        } catch (Exception e) {
            socketService.broadcastResponse(userID, roomId,false, "ready" + userID, receiptId);
        }
    }

    //set unready
    @MessageMapping("/message/users/unready/{roomId}")
    public void unready(SimpMessageHeaderAccessor headerAccessor, @DestinationVariable("roomId") String roomId, @Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receiptId = null; // Initialize receiptId
        String userID = payload.getMessage().getUserID();
    
        // Try to extract receiptId from nativeHeaders
        @SuppressWarnings("unchecked")
        Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) headerAccessor.getHeader("nativeHeaders");
        if (nativeHeaders != null && nativeHeaders.containsKey("receiptId")) {
            receiptId = nativeHeaders.get("receiptId").get(0);
            System.out.println("Receipt ID found for roomId: " + roomId);
        } else {
            System.out.println("Receipt ID not found for roomId: " + roomId);
        }

        System.out.println("[Unready msg received] RoomID: " + roomId + ", UserID: " + userID);
    
        try {
            gameService.UnReady(userID);
            socketService.broadcastResponse(userID, roomId, true, "unready " + userID, receiptId);
            socketService.broadcastPlayerInfo(roomId, receiptId);
            socketService.broadcastGameinfo(roomId, receiptId);
            // socketService.broadcastLobbyInfo();
        } catch (Exception e) {
            socketService.broadcastResponse(userID, roomId,false, "ready" + userID, receiptId);
        }
    }


    //enterroom
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
            socketService.broadcastLobbyInfo();
        }
    }


    @MessageMapping("/message/users/exitroom/{roomId}")
    public void exitRoom(SimpMessageHeaderAccessor headerAccessor, @DestinationVariable("roomId") String roomID, @Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receiptID = null; // Initialize receiptId
        String userID = payload.getMessage().getUserID();

        // Try to extract receiptId from nativeHeaders
        @SuppressWarnings("unchecked")
        Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) headerAccessor.getHeader("nativeHeaders");
        if (nativeHeaders != null && nativeHeaders.containsKey("receiptId")) {
            receiptID = nativeHeaders.get("receiptId").get(0);
            System.out.println("Receipt ID found for roomId: " + roomID);
        } else {
            System.out.println("Receipt ID not found for roomId: " + roomID);
        }

        System.out.println("[exitRoom msg received] RoomID: " + roomID + ", UserID: " + userID);

        try {
            Room room=roomService.findRoomById(userID,roomID); // Assumes findRoomById only needs roomId
            User user = userService.findUserById(userID);

            if (room != null && room.getRoomPlayersList().contains(user.getId())) {
                roomService.exitRoom(room, user);
                // socketService.broadcastGameinfo(roomID, receiptID);
                socketService.broadcastPlayerInfo(roomID, "exitroom");
                socketService.broadcastLobbyInfo();
                socketService.broadcastResponse(userID, roomID, true, "Successfully exited room", receiptID);
            } else {
                socketService.broadcastResponse(userID, roomID, false, "Failed to exit room", receiptID);
            }
        } catch (Exception e) {
            // Log error or handle exception
            e.printStackTrace(); 
            System.out.println("Error exiting room: " + e.getMessage());
            socketService.broadcastLobbyInfo();
            socketService.broadcastResponse(userID, roomID, false, "Failed to exit room: " + e.getMessage(), receiptID);
        }
    }


    // Method for starting the game
    @MessageMapping("/message/games/start/{roomId}")
    public void startGame(SimpMessageHeaderAccessor headerAccessor, @DestinationVariable("roomId") String roomID, @Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receiptID = null; // Initialize receiptId
        String userID = payload.getMessage().getUserID();

        // Try to extract receiptId from nativeHeaders
        @SuppressWarnings("unchecked")
        Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) headerAccessor.getHeader("nativeHeaders");
        if (nativeHeaders != null && nativeHeaders.containsKey("receiptId")) {
            receiptID = nativeHeaders.get("receiptId").get(0);
            System.out.println("Receipt ID found for roomId: " + roomID);
        } else {
            System.out.println("Receipt ID not found for roomId: " + roomID);
        }

        try {
            Room room=roomService.findRoomById(userID,roomID);  // Adjusted to use roomId directly if applicable
            gameService.checkIfAllReady(room);  // Checks if all players in the room are ready
            socketService.broadcastResponse(userID, roomID, true, "Game started successfully", receiptID);
            gameService.startGame(room);  // Starts the game
            // Send success response back to user
        } catch (Exception e) {
            // Handle errors during game start, such as not all players being ready
            System.out.println("Error starting game: " + e.getMessage());
            socketService.broadcastResponse(userID, roomID, false, "Failed to start game: " + e.getMessage(), receiptID);
        }
    }
    //submitAnswer
    @MessageMapping("/message/games/validate/{roomId}")
    public void submitAnswer(SimpMessageHeaderAccessor headerAccessor, @DestinationVariable("roomId") String roomID, @Payload TimestampedRequest<AnswerGuess> payload) {
        String receiptID = null;
        String userID = payload.getMessage().getUserID();
        String guess = payload.getMessage().getGuess();
    
        @SuppressWarnings("unchecked")
        Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) headerAccessor.getHeader("nativeHeaders");
        if (nativeHeaders != null && nativeHeaders.containsKey("receiptId")) {
            receiptID = nativeHeaders.get("receiptId").get(0);
        }
    
        try {
            Game game = gameService.findGameById(roomID);
            Player player = playerService.findPlayerById(userID);
            gameService.validateAnswer(game, player, guess);
            socketService.broadcastResponse(userID, roomID, true, "Answer submitted successfully", receiptID);
        } catch (Exception e) {
            System.out.println("Error submitting answer: " + e.getMessage());
            socketService.broadcastResponse(userID, roomID, false, "Failed to submit answer: " + e.getMessage(), receiptID);
        }
    }

    //submitAudio
    @MessageMapping("/message/games/audio/upload/{roomId}")
    public void uploadAudio(SimpMessageHeaderAccessor headerAccessor, @DestinationVariable("roomId") String roomId, @Payload TimestampedRequest<PlayerAudio> payload) {
        String receiptId = null;
        String userId = payload.getMessage().getUserID();
        String voice = payload.getMessage().getAudioData();

        @SuppressWarnings("unchecked")
        Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) headerAccessor.getHeader("nativeHeaders");
        if (nativeHeaders != null && nativeHeaders.containsKey("receiptId")) {
            receiptId = nativeHeaders.get("receiptId").get(0);
        }

        try {
            gameService.setPlayerAudio(roomId, userId, voice);
            socketService.broadcastResponse(userId, roomId, true, "Audio uploaded successfully", receiptId);
        } catch (Exception e) {
            System.out.println("Error uploading audio: " + e.getMessage());
            socketService.broadcastResponse(userId, roomId, false, "Failed to upload audio: " + e.getMessage(), receiptId);
        }
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
