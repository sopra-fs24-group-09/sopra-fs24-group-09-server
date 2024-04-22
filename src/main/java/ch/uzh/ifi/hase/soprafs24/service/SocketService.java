package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.RoomProperty;
import ch.uzh.ifi.hase.soprafs24.model.TimestampedRequest;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.entity.User;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SocketService {

    @Autowired
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RoomRepository roomRepository;

    public SocketService(SimpMessagingTemplate simpMessagingTemplate,
            @Qualifier("userRepository") UserRepository userRepository,
            @Qualifier("playerRepository") PlayerRepository playerRepository,
            @Qualifier("roomRepository") RoomRepository roomRepository,
            @Qualifier("gameRepository") GameRepository gameRepository, RoomService roomService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.roomRepository = roomRepository;
    }

    // helper function for sending message to destination with JSON format
    private <T> void sendMessage(String destination, String roomId, T info, String receiptId) {
        try {
            // Wrapping the info object within Timestamped
            TimestampedRequest<T> timestampedMessage = new TimestampedRequest<>();
            timestampedMessage.setTimestamp(Instant.now().toEpochMilli()); // Assuming you want current time in UTC
                                                                           // milliseconds
            timestampedMessage.setMessage(info);
            System.out.println(info);

            // The payload to be sent is now the timestampedMessage object
            simpMessagingTemplate.convertAndSend(destination, timestampedMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // broadcast game info message
    public void broadcastGameinfo(String roomId, String receipId) {
        // Optional<Game> optionalGame = gameRepository.findByRoomId(roomId);
        // Game game = optionalGame.orElseThrow(() ->
        // new IllegalStateException("No game found with room ID: " + roomId));
        Room room = roomRepository.findByRoomId(roomId).get();
        UserGetDTO roomOwnerDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(room.getRoomOwner());
        HashMap<String, Object> info = new HashMap<>();
        info.put("roomID", room.getRoomId());
        info.put("theme", room.getTheme());
        info.put("roomOwner", roomOwnerDTO);
        info.put("gameStatus", room.getRoomProperty());

        if (room.getRoomProperty().equals(RoomProperty.WAITING)) {
            info.put("currentSpeaker", "None");
            info.put("currentAnswer", "None");
            info.put("roundStatus", "None");
            info.put("roundDue", "None");
            info.put("currentRoundNum", "None");
        }
        else{
            Game game = new Game(room);

            PlayerGetDTO currentSpeakerDTO = DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(game.getCurrentSpeaker());

            info.put("currentSpeaker", currentSpeakerDTO);
            info.put("currentAnswer", game.getCurrentAnswer());
            info.put("roundStatus", game.getRoundStatus());
            info.put("roundDue", game.getRoundDue());
            info.put("currentRoundNum", game.getCurrentRoundNum());
        }

        sendMessage("/games/info", roomId, info, receipId);
    }

    // broadcast player info message
    public void broadcastPlayerInfo(String roomId, String userId, String receipId) {
        // Optional<Player> optionalPlayer = playerRepository.findById(userId);
        // if (!optionalPlayer.isPresent()) {
        // throw new IllegalStateException("No player with ID: " + userId);
        // }
        // Player player = optionalPlayer.get();
        User user = new User();
        Player player = new Player(user);
        player.setId("1");
        player.setUsername("1");
        player.setAvatar("1");
        // define user information
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("id", player.getId());
        userMap.put("name", player.getUsername());
        userMap.put("avatar", player.getAvatar());

        List<Map<String, Object>> scoreDetails = player.getScoreDetails();
        // define score information
        HashMap<String, Object> scoreMap = new HashMap<>();
        scoreMap.put("total", player.getTotalScore());
        scoreMap.put("guess", player.getGuessScore());
        scoreMap.put("read", player.getSpeakScore());
        scoreMap.put("details", scoreDetails);

        // final structure for playerinfo
        HashMap<String, Object> infoMap = new HashMap<>();
        infoMap.put("user", userMap);
        infoMap.put("score", scoreMap);
        infoMap.put("ready", true);
        infoMap.put("ifGuess", player.isIfGuessed());
        infoMap.put("roundFinished", player.isRoundFinished());

        sendMessage("/plays/info", roomId, infoMap, receipId);
    }

    public void broadcastSpeakerAudio(String roomId, String userId, String voice) {
        HashMap<String, Object> info = new HashMap<>();
        info.put("userId", userId);
        info.put("roomId", roomId);
        info.put("audioData", voice);

        sendMessage("/plays/audio", roomId, info, null);
    }

    public void broadcastAudio(String roomId, Map<String, String> VoiceDict) {
        HashMap<String, Object> info = new HashMap<>();
        info.put("listOfaudioData", VoiceDict);
        sendMessage("/plays/audio", roomId, info, null);
    }

}
