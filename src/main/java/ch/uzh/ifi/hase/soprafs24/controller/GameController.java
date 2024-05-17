package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.annotation.UserLoginToken;
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
    private RoomRepository roomRepository;

    public GameController(RoomService roomService, SocketService socketService, UserService userService, PlayerService playerService, GameService gameService, SimpMessagingTemplate simpMessagingTemplate, @Qualifier("gameRepository") GameRepository gameRepository, @Qualifier("playerRepository") PlayerRepository playerRepository, @Qualifier("roomRepository") RoomRepository roomRepository){
        this.socketService = socketService;
        this.gameService=gameService;
        this.roomService = roomService;
        this.userService = userService;
        this.gameRepository = gameRepository;
        this.playerService = playerService;
        this.playerRepository = playerRepository;
        this.playerRepository = playerRepository;
        this.roomRepository = roomRepository;
    }

    //set ready
    @UserLoginToken
    @MessageMapping("/message/users/ready/{roomId}")
    public void ready(SimpMessageHeaderAccessor headerAccessor, @DestinationVariable("roomId") String roomId, @Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receiptId = null; // Initialize receiptId
        String token = null;
        String userID = payload.getMessage().getUserID();

        // Try to extract receiptId from nativeHeaders
        @SuppressWarnings("unchecked")
        Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) headerAccessor.getHeader("nativeHeaders");
        if (nativeHeaders != null && nativeHeaders.containsKey("receiptId")) {
            receiptId = nativeHeaders.get("receiptId").get(0);
            token = nativeHeaders.get("token").get(0);
            System.out.println("Receipt ID found for roomId: " + roomId);
        }

        try {
            if (token == null || !userService.findByToken(token)) {
                // Token is invalid or expired, send a response with auth set to false
                socketService.broadcastResponse(userID, roomId, false, false, "Invalid or expired token", receiptId);
                return; // Stop further processing
            }
            gameService.Ready(userID, roomId);
            socketService.broadcastResponse(userID, roomId, true, true,"unready " + userID, receiptId);
            socketService.broadcastPlayerInfo(roomId, receiptId);
            socketService.broadcastGameinfo(roomId, receiptId);
            // socketService.broadcastLobbyInfo();
        } catch (Exception e) {
            socketService.broadcastResponse(userID, roomId,false, true,  "Failed to unready: " + e.getMessage(), receiptId);
        }
    }

    //set unready
    @UserLoginToken
    @MessageMapping("/message/users/unready/{roomId}")
    public void unready(SimpMessageHeaderAccessor headerAccessor, @DestinationVariable("roomId") String roomId, @Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receiptId = null; // Initialize receiptId
        String token = null;
        String userID = payload.getMessage().getUserID();
    
        // Try to extract receiptId from nativeHeaders
        @SuppressWarnings("unchecked")
        Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) headerAccessor.getHeader("nativeHeaders");
        if (nativeHeaders != null && nativeHeaders.containsKey("receiptId")) {
            receiptId = nativeHeaders.get("receiptId").get(0);
            token = nativeHeaders.get("token").get(0);
        }
    
        try {
            if (token == null || !userService.findByToken(token)) {
                // Token is invalid or expired, send a response with auth set to false
                socketService.broadcastResponse(userID, roomId, false, false, "Invalid or expired token", receiptId);
                return; // Stop further processing
            }
            gameService.UnReady(userID);
            socketService.broadcastResponse(userID, roomId, true, true,"unready " + userID, receiptId);
            socketService.broadcastPlayerInfo(roomId, receiptId);
            socketService.broadcastGameinfo(roomId, receiptId);
            // socketService.broadcastLobbyInfo();
        } catch (Exception e) {
            socketService.broadcastResponse(userID, roomId,false, true,  "Failed to unready: " + e.getMessage(), receiptId);
        }
    }


    //enterroom
    @UserLoginToken
    @MessageMapping("/message/users/enterroom/{roomId}")
    public void enterRoom(SimpMessageHeaderAccessor headerAccessor, @DestinationVariable("roomId") String roomId, @Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receiptID = null; // Initialize receiptId
        String token = null;
        String userID = payload.getMessage().getUserID();

        // Try to extract receiptId from nativeHeaders
        @SuppressWarnings("unchecked")
        Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) headerAccessor.getHeader("nativeHeaders");
        if (nativeHeaders != null && nativeHeaders.containsKey("receiptId")) {
            receiptID = nativeHeaders.get("receiptId").get(0);
            token = nativeHeaders.get("token").get(0);
        }

        try {
            if (token == null || !userService.findByToken(token)) {
                // Token is invalid or expired, send a response with auth set to false
                socketService.broadcastResponse(userID, roomId, false, false, "Invalid or expired token", receiptID);
                return; // Stop further processing
            }
            else {
                if (userService.findUserById(userID).getInRoomId() != null && !roomRepository.findByRoomId(userService.findUserById(userID).getInRoomId()).isPresent()) {
                    userService.findUserById(userID).setInRoomId(null);
                }
                if (roomRepository.findByRoomId(roomId).isPresent()) {
                    //if the user is already in the room
                    if(roomRepository.findByRoomId(roomId).isEmpty()){
                        throw new Exception("Room not found");
                    }
                    Room room = roomRepository.findByRoomId(roomId).get();
                    User user = userService.findUserById(userID);
                    //if the user is already in the room
                    if (room.getRoomPlayersList().contains(user.getId())) {
                        //if the game is started and the user is entering the room
                        if (room.getRoomProperty().equals(RoomProperty.INGAME)) {

                            Game game = gameRepository.findByRoomId(room.getRoomId()).orElseThrow(() -> new Exception("Game not found"));
                            //if the game is in the guess round
                            if (game.getRoundStatus().equals(RoundStatus.guess)) {
                                if (playerRepository.findById(game.getCurrentSpeaker().getId()).isEmpty()) {
                                    throw new Exception("Cannot find player");
                                }
                                String voice = playerRepository.findById(game.getCurrentSpeaker().getId()).get().getAudioData();
                                socketService.broadcastGameinfo(roomId, receiptID);
                                socketService.broadcastPlayerInfo(roomId, "enterroom");
                                socketService.broadcastLobbyInfo();
                                socketService.broadcastSpeakerAudio(game.getRoomId(), game.getCurrentSpeaker().getId(), voice);
                            }
                        }
                        //if the game is not started and the user is entering the room
                        socketService.broadcastGameinfo(roomId, receiptID);
                        socketService.broadcastPlayerInfo(roomId, "enterroom");
                        socketService.broadcastLobbyInfo();
                        socketService.broadcastResponse(userID, roomId, true, true, "enter room", receiptID);

                    }
                    //if the user is not in the room
                    else {
                        System.out.println("User " + user.getUsername() + " is entering room " + room.getRoomId());
                        roomService.enterRoom(room, user);
                        socketService.broadcastGameinfo(roomId, receiptID);
                        socketService.broadcastPlayerInfo(roomId, "enterRoom");
                        socketService.broadcastLobbyInfo();
                        socketService.broadcastResponse(userID, roomId, true,true, "enter room", receiptID);
                    }
                }
                else {
                    throw new Exception("Room not found");
                }
            }

        } catch (Exception e) {
            // Log error or handle exception
            System.out.println("Error entering room: " + e.getMessage());
            socketService.broadcastResponse(userID, roomId, false,true, e.getMessage(), receiptID);
            socketService.broadcastLobbyInfo();
        }
    }

    //leaveroom
    @UserLoginToken
    @MessageMapping("/message/users/exitroom/{roomId}")
    public void exitRoom(SimpMessageHeaderAccessor headerAccessor, @DestinationVariable("roomId") String roomID, @Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receiptID = null; // Initialize receiptId
        String token = null;
        String userID = payload.getMessage().getUserID();

        // Try to extract receiptId from nativeHeaders
        @SuppressWarnings("unchecked")
        Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) headerAccessor.getHeader("nativeHeaders");
        if (nativeHeaders != null && nativeHeaders.containsKey("receiptId")) {
            receiptID = nativeHeaders.get("receiptId").get(0);
            token = nativeHeaders.get("token").get(0);
        }

        try {
            if (token == null || !userService.findByToken(token)) {
                // Token is invalid or expired, send a response with auth set to false
                socketService.broadcastResponse(userID, roomID, false, false, "Invalid or expired token", receiptID);
                return; // Stop further processing
            }
            else {
                if (roomRepository.findByRoomId(roomID).isPresent()) {
                    if (roomRepository.findByRoomId(roomID).isEmpty()) {
                        throw new Exception("Room not found");
                    }
                    Room room = roomRepository.findByRoomId(roomID).get();
                    User user = userService.findUserById(userID);
                    if (room.getRoomProperty().equals(RoomProperty.INGAME)) {
                        throw new Exception("Cannot exit room while game is in progress");
                    }
                    if (room.getRoomPlayersList().contains(user.getId())) {
                        roomService.exitRoom(room, user);
                        socketService.broadcastLobbyInfo();

                        if (roomRepository.findByRoomId(roomID).isPresent()) {
                            socketService.broadcastPlayerInfo(roomID, "exitroom");
                            socketService.broadcastGameinfo(roomID, receiptID);
                            socketService.broadcastResponse(userID, roomID, true,true, "Successfully exited room", receiptID);
                        }
                    }
                    else {
                        socketService.broadcastResponse(userID, roomID, false,true, "Failed to exit room", receiptID);
                    }
                }
            }
        } catch (Exception e) {
            // Log error or handle exception
            // e.printStackTrace(); 
            System.out.println("Error exiting room: " + e.getMessage());
            socketService.broadcastLobbyInfo();
            socketService.broadcastResponse(userID, roomID, false,true, "Failed to exit room: " + e.getMessage(), receiptID);
        }
    }

    //startgame
    @UserLoginToken
    @MessageMapping("/message/games/start/{roomId}")
    public void startGame(SimpMessageHeaderAccessor headerAccessor, @DestinationVariable("roomId") String roomID, @Payload TimestampedRequest<PlayerAndRoom> payload) {
        String receiptID = null; // Initialize receiptId
        String token = null;
        String userID = payload.getMessage().getUserID();

        // Try to extract receiptId from nativeHeaders
        @SuppressWarnings("unchecked")
        Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) headerAccessor.getHeader("nativeHeaders");
        if (nativeHeaders != null && nativeHeaders.containsKey("receiptId")) {
            receiptID = nativeHeaders.get("receiptId").get(0);
            token = nativeHeaders.get("token").get(0);
        }

        try {
            if (token == null || !userService.findByToken(token)) {
                // Token is invalid or expired, send a response with auth set to false
                socketService.broadcastResponse(userID, roomID, false, false, "Invalid or expired token", receiptID);
                return; // Stop further processing
            }
            else {
                if (!roomRepository.findByRoomId(roomID).isPresent()) {
                    throw new Exception("Room not found");
                }
                else {
                    if (roomRepository.findByRoomId(roomID).isEmpty()) {
                        throw new Exception("Room not found");
                    }

                    Room room = roomRepository.findByRoomId(roomID).get();
                    if (room.getRoomWordsList() == null || room.getRoomWordsList().size() == 0) {
                        throw new Exception("Room does not have enough words to start game");
                    }
                    else {
                        gameService.checkIfAllReady(room);  // Checks if all players in the room are ready
                        socketService.broadcastResponse(userID, roomID, true, true, "Game started successfully", receiptID);
                        gameService.startGame(room);  // Starts the game
                        // Send success response back to user
                    }
                    // Send success response back to user
                }
            }
        } catch (Exception e) {
            // Handle errors during game start, such as not all players being ready
            System.out.println("Error starting game: " + e.getMessage());
            socketService.broadcastResponse(userID, roomID, false,true, "Failed to start game: " + e.getMessage(), receiptID);
        }
    }
    //submitAnswer
    @UserLoginToken
    @MessageMapping("/message/games/validate/{roomId}")
    public void submitAnswer(SimpMessageHeaderAccessor headerAccessor, @DestinationVariable("roomId") String roomID, @Payload TimestampedRequest<AnswerGuess> payload) {
        String receiptID = null;
        String userID = payload.getMessage().getUserID();
        String token = null;
        String guess = payload.getMessage().getGuess();
        
        // Remove special characters and space from guess
        guess = guess.replaceAll("[^a-zA-Z0-9]", "");
        System.out.println("user upload guess:"+ guess );
        @SuppressWarnings("unchecked")
        Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) headerAccessor.getHeader("nativeHeaders");
        if (nativeHeaders != null && nativeHeaders.containsKey("receiptId")) {
            receiptID = nativeHeaders.get("receiptId").get(0);
            token = nativeHeaders.get("token").get(0);
        }
    
        try {
            if (token == null || !userService.findByToken(token)) {
                // Token is invalid or expired, send a response with auth set to false
                socketService.broadcastResponse(userID, roomID, false, false, "Invalid or expired token", receiptID);
                return; // Stop further processing
            }
            else {
                Game game = gameService.findGameById(roomID);
                Player player = playerService.findPlayerById(userID);
                gameService.validateAnswer(game, player, guess);
                socketService.broadcastResponse(userID, roomID, true,true, "Answer submitted successfully", receiptID);
            }
        } catch (Exception e) {
            System.out.println("Error submitting answer: " + e.getMessage());
            socketService.broadcastResponse(userID, roomID, false,true, "Failed to validate answer: " + e.getMessage(), receiptID);
        }
    }

    //submitAudio
    @UserLoginToken
    @MessageMapping("/message/games/audio/upload/{roomId}")
    public void uploadAudio(SimpMessageHeaderAccessor headerAccessor, @DestinationVariable("roomId") String roomId, @Payload TimestampedRequest<PlayerAudio> payload) {
        String receiptId = null;
        String token = null;
        String userId = payload.getMessage().getUserID();
        String voice = payload.getMessage().getAudioData();
        System.out.println("user upload voice:"+ voice );

        @SuppressWarnings("unchecked")
        Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) headerAccessor.getHeader("nativeHeaders");
        if (nativeHeaders != null && nativeHeaders.containsKey("receiptId")) {
            receiptId = nativeHeaders.get("receiptId").get(0);
            token = nativeHeaders.get("token").get(0);
        }

        try {
            if (token == null || !userService.findByToken(token)) {
                // Token is invalid or expired, send a response with auth set to false
                socketService.broadcastResponse(userId, roomId, false, false, "Invalid or expired token", receiptId);
                return; // Stop further processing
            }
            else {
                gameService.setPlayerAudio(roomId, userId, voice);
                socketService.broadcastResponse(userId, roomId, true,true, "Audio uploaded successfully", receiptId);
            }
        } catch (Exception e) {
            System.out.println("Error uploading audio: " + e.getMessage());
            socketService.broadcastResponse(userId, roomId, false,true, "Failed to upload audio: " + e.getMessage(), receiptId);
        }
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
