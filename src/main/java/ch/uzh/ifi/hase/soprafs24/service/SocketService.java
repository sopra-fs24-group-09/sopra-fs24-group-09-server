package ch.uzh.ifi.hase.soprafs24.service;
import ch.uzh.ifi.hase.soprafs24.model.Message;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import ch.uzh.ifi.hase.soprafs24.constant.MessageOrderType;
import ch.uzh.ifi.hase.soprafs24.constant.Theme;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.entity.User;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class SocketService {

    @Autowired
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;

    private final ObjectMapper objectMapper;
    public SocketService(SimpMessagingTemplate simpMessagingTemplate, @Qualifier("userRepository") UserRepository userRepository,@Qualifier("playerRepository") PlayerRepository playerRepository,@Qualifier("gameRepository") GameRepository gameRepository, ObjectMapper objectMapper, RoomService roomService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    private void sendMessage(String roomId, Object info, MessageOrderType messageType) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(info);
            
            Message message = new Message();
            message.setSenderName("system");
            message.setTimestamp(LocalDateTime.now());
            message.setMessageType(messageType);
            message.setMessage(jsonMessage);

            simpMessagingTemplate.convertAndSend("/test", message);
            simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", message);
        } catch (Exception e) {
            e.printStackTrace(); // Consider more nuanced error handling based on your application's needs
        }
    }

    // public method for system reminder
    public void systemReminder(String reminderInfo,Long roomId) {
        Message reminderMessage = new Message();
        reminderMessage.setSenderName("system");
        reminderMessage.setMessage(reminderInfo);
        reminderMessage.setMessageType(MessageOrderType.MESSAGE);
        simpMessagingTemplate.convertAndSend("/room/"+roomId+"/public", reminderMessage);
    }
    
    //broadcast ready message
    public void broadcastReady(String roomId, boolean isReady) {
        Message readinessMessage = new Message();
        readinessMessage.setSenderName("system");
        readinessMessage.setTimestamp(LocalDateTime.now());
        readinessMessage.setMessageType(MessageOrderType.READY); 
        readinessMessage.setMessage(isReady ? "Ready" : "Not Ready");
        simpMessagingTemplate.convertAndSend("/test", readinessMessage);
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", readinessMessage);
    }

    //broadcast unready message
    public void broadcastUnReady(String roomId, boolean isReady) {
        Message readinessMessage = new Message();
        readinessMessage.setSenderName("system");
        readinessMessage.setTimestamp(LocalDateTime.now());
        readinessMessage.setMessageType(MessageOrderType.UNREADY); 
        readinessMessage.setMessage(isReady ? "Ready" : "Not Ready");
        simpMessagingTemplate.convertAndSend("/test", readinessMessage);
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", readinessMessage);
    }

    //broadcast enterroom message
    public void broadcastEnterroom(String roomId,String userid) {
        Message readinessMessage = new Message();
        readinessMessage.setSenderName("system");
        readinessMessage.setTimestamp(LocalDateTime.now());
        readinessMessage.setMessageType(MessageOrderType.ENTER_ROOM); 
        Optional<User> userOptional = userRepository.findById(userid);
        String message;
        if ( userOptional.isPresent()) {
            User user = userOptional.get();
            message = user.getUsername() + " has entered the room.";
        } else {
            message = "Unknown user entered the room.";
        }
        readinessMessage.setMessage(message);
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", readinessMessage);
    }


    //broadcast enterroom message
    public void broadcastExitroom(String roomId,String userid) {
        Message readinessMessage = new Message();
        readinessMessage.setSenderName("system");
        readinessMessage.setTimestamp(LocalDateTime.now());
        readinessMessage.setMessageType(MessageOrderType.EXIT_ROOM); 
        Optional<User> userOptional = userRepository.findById(userid);
        String message;
        if ( userOptional.isPresent()) {
            User user = userOptional.get();
            message = user.getUsername() + " has left the room.";
        } else {
            message = "Unknown user left the room.";
        }
        readinessMessage.setMessage(message);
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", readinessMessage);
    }

    //broadcast game start message
    public void broadcastGamestart(String roomId) {
        Message readinessMessage = new Message();
        readinessMessage.setSenderName("system");
        readinessMessage.setTimestamp(LocalDateTime.now());
        readinessMessage.setMessageType(MessageOrderType.GAME_START); 
        readinessMessage.setMessage("Game Start!");
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", readinessMessage);
    }


    //broadcast room infor message
    public void broadcastRoominfo(String roomId) {
        // Room room = roomService.findRoomById(roomId);
        Room room = new Room();
        room.setRoomId("1");
        User user = new User();
        user.setId("1");
        room.setRoomOwner(user);
        room.setTheme(Theme.FURNITURE);
        HashMap<String, Object> info = new HashMap<>();
        info.put("roomId", room.getRoomId());
        // info.put("status", room.getRoomProperty());
        // info.put("playerReadyStatus", room.getRoomPlayersList());
        info.put("roomOwner", room.getRoomOwner());
        info.put("theme", room.getTheme());
        sendMessage(roomId, info, MessageOrderType.ROOM_INFO);
    }

    //broadcast game info message
    public void broadcastGameinfo(String roomId) {
        // Optional<Game> optionalGame = gameRepository.findByRoomId(roomId);
        // Game game = optionalGame.orElseThrow(() -> 
        // new IllegalStateException("No game found with room ID: " + roomId));
        Room room = new Room();
        Game game = new Game(room);
        User user = new User();
        Player player = new Player(user);

        game.setRoomId("1");
        game.setCurrentSpeaker(player);
        game.setCurrentAnswer("Test answer");
        PlayerGetDTO currentSpeakerDTO = DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(game.getCurrentSpeaker());

        HashMap<String, Object> info = new HashMap<>();
        info.put("roomID", game.getRoomId());
        info.put("currentSpeaker", currentSpeakerDTO);
        info.put("currentAnswer", game.getCurrentAnswer());
        info.put("roundStatus", game.getRoundStatus());
        info.put("currentRoundNum", game.getCurrentRoundNum());
        // info.put("playerScores", player.getScoreDetails());
                   
        sendMessage(roomId, info, MessageOrderType.GAME_INFO);
    }


    //broadcast player words message
    public void broadcastPlayerinfo(String roomId, String userId) {
        // Optional<Player> optionalPlayer = playerRepository.findById(userId);
        // Player player = optionalPlayer.orElseThrow(() -> 
        // new IllegalStateException("No player with ID: " + userId));
        User user = new User();
        Player player = new Player(user);
        HashMap<String, Object> info = new HashMap<>();
        info.put("userId", userId);
        info.put("roomId", roomId);
        info.put("word", player.getAssignedWord());
        
        sendMessage(roomId, info, MessageOrderType.PLAYER_INFO);
    }

    public void broadcastSpeakerAudio(String roomId, String userId, String voice) {
        HashMap<String, Object> info = new HashMap<>();
        info.put("userId", userId);
        info.put("audioData", voice);
    
        sendMessage(roomId, info, MessageOrderType.NOTIFY_SPEAKER_AUDIO);
    }

    public void broadcastAudio(String roomId, Map<String, String> Voice) {
        HashMap<String, Object> info = new HashMap<>();
        info.put("roomId", roomId);
        info.put("audioData", Voice);
        
        sendMessage(roomId, info, MessageOrderType.NOTIFY_AUDIO);
    }

}
