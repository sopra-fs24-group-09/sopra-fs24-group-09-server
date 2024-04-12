package ch.uzh.ifi.hase.soprafs24.service;
import ch.uzh.ifi.hase.soprafs24.model.Message;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import ch.uzh.ifi.hase.soprafs24.constant.MessageOrderType;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.entity.User;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class SocketService {

    @Autowired
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final RoomService roomService;
    private final GameService gameService;

    public SocketService(SimpMessagingTemplate simpMessagingTemplate, UserRepository userRepository, ObjectMapper objectMapper, RoomService roomService, GameService gameService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.roomService = roomService;
        this.gameService = gameService;
    }

    private void sendMessage(String roomId, Object info, MessageOrderType messageType) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(info);
            
            Message readinessMessage = new Message();
            readinessMessage.setSenderName("system");
            readinessMessage.setTimestamp(LocalDateTime.now());
            readinessMessage.setMessageType(messageType);
            readinessMessage.setMessage(jsonMessage);
            
            simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", readinessMessage);
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
    public void broadcastReady(Long roomId, boolean isReady) {
        Message readinessMessage = new Message();
        readinessMessage.setSenderName("system");
        readinessMessage.setSenderName("system");
        readinessMessage.setTimestamp(LocalDateTime.now());
        readinessMessage.setMessageType(MessageOrderType.READY); 
        readinessMessage.setMessage(isReady ? "Ready" : "Not Ready");
        
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", readinessMessage);
    }

    //broadcast unready message
    public void broadcastUnReady(Long roomId, boolean isReady) {
        Message readinessMessage = new Message();
        readinessMessage.setTimestamp(LocalDateTime.now());
        readinessMessage.setMessageType(MessageOrderType.UNREADY); 
        readinessMessage.setMessage(isReady ? "Ready" : "Not Ready");
        
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
        Room room = roomService.findRoomById(roomId);
        HashMap<String, Object> info = new HashMap<>();
        info.put("roomId", room.getRoomId());
        info.put("status", room.getRoomProperty());
        info.put("playerReadyStatus", room.getRoomPlayersList());
        
        sendMessage(roomId, info, MessageOrderType.ROOM_INFO);
    }

    //broadcast game info message
    public void broadcastGameinfo(String roomId) {
        Game game = gameService.findGameById(roomId);
        HashMap<String, Object> info = new HashMap<>();
        info.put("roomId", game.getRoomId());
        info.put("currentSpeakerID", game.getCurrentSpeaker());
        info.put("currentAnswer", game.getCurrentAnswer());
        info.put("roundStatus", game.getRoundStatus());
        info.put("currentRoundNum", game.getCurrentRoundNum());
        info.put("playerScores", game.getPlayerScores());
        
        sendMessage(roomId, info, MessageOrderType.GAME_INFO);
    }


    //broadcast player words message
    public void broadcastPlayerwords(String roomId, String userId) {
        Player player = gameService.findPlayerInGame(userId, roomId);
        HashMap<String, Object> info = new HashMap<>();
        info.put("userId", userId);
        info.put("roomId", roomId);
        info.put("wordsEachRound", player.getWordsEachRound());
        
        sendMessage(roomId, info, MessageOrderType.PLAYER_WORDS);
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
