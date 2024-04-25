package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.PlayerStatus;
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
import java.util.ArrayList;
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
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;

    public SocketService(SimpMessagingTemplate simpMessagingTemplate,
            @Qualifier("userRepository") UserRepository userRepository,
            @Qualifier("playerRepository") PlayerRepository playerRepository,
            @Qualifier("roomRepository") RoomRepository roomRepository,
            @Qualifier("gameRepository") GameRepository gameRepository, RoomService roomService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
    }

    // helper function for sending message to destination with JSON format
    private <T> void sendMessage(String destination, String roomId, T info, String receiptId) {
        try {
            // Wrapping the info object within Timestamped
            TimestampedRequest<T> timestampedMessage = new TimestampedRequest<>();
            timestampedMessage.setTimestamp(Instant.now().toEpochMilli()); // Assuming you want current time in UTC
                                                                           // milliseconds
            timestampedMessage.setMessage(info);
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
        User roomowner = userRepository.findById(room.getRoomOwnerId()).get();
        UserGetDTO roomOwnerDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(roomowner);
        HashMap<String, Object> info = new HashMap<>();
        info.put("roomID", room.getRoomId());
        info.put("theme", room.getTheme());
        info.put("roomOwner", roomOwnerDTO);
//        info.put("gameStatus", room.getRoomProperty());


        if (room.getRoomProperty().equals(RoomProperty.WAITING)) {
            info.put("currentSpeaker", "None");
            info.put("currentAnswer", "None");
            info.put("roundStatus", "None");
            info.put("roundDue", "None");
            info.put("currentRoundNum", "None");
            info.put("gameStatus", "ready");
        }
        else{
            Game game = gameRepository.findByRoomId(roomId).get();
            PlayerGetDTO currentSpeakerDTO = DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(game.getCurrentSpeaker());

            info.put("currentSpeaker", currentSpeakerDTO);
            info.put("currentAnswer", game.getCurrentAnswer());
            info.put("roundStatus", game.getRoundStatus());
            info.put("roundDue", game.getRoundDue());
            info.put("currentRoundNum", game.getCurrentRoundNum());
            info.put("gameStatus", game.getGameStatus());
        }
        sendMessage( "/games/info/" + roomId, roomId, info, receipId);
    }

    // broadcast player info message
    public void broadcastPlayerInfo(String roomId, String receipId) {
        Room room = roomRepository.findByRoomId(roomId).get();
        List<Map<String, Object>> infoMap_total = new ArrayList<>();
        for (String id : room.getRoomPlayersList()){
            HashMap<String, Object> userMap = new HashMap<>();
            HashMap<String, Object> infoMap = new HashMap<>();
            HashMap<String, Object> scoreMap = new HashMap<>();
            //user is always the same
            User user = userRepository.findById(id).get();
            userMap.put("id", user.getId());
            userMap.put("name", user.getUsername());
            userMap.put("avatar", user.getAvatar());
            // Before game starts
            if (room.getRoomProperty().equals(RoomProperty.WAITING)) {
                scoreMap.put("total", 0);
                scoreMap.put("guess", 0);
                scoreMap.put("read", 0);
                scoreMap.put("details", 0);

                infoMap.put("user", userMap);
                infoMap.put("score", scoreMap);
                infoMap.put("ready", user.getPlayerStatus().equals(PlayerStatus.READY));
                infoMap.put("ifGuess", null);
                infoMap.put("roundFinished", null);
            }
            // After game starts
            else {
                Player player = playerRepository.findById(id).get();
                List<Map<String, Object>> scoreDetails = player.getScoreDetails();
                scoreMap.put("total", player.getTotalScore());
                scoreMap.put("guess", player.getGuessScore());
                scoreMap.put("read", player.getSpeakScore());
                scoreMap.put("details", scoreDetails);

                infoMap.put("user", userMap);
                infoMap.put("score", scoreMap);
                infoMap.put("ready", true);
                infoMap.put("ifGuess", player.getIfGuessed());
                infoMap.put("roundFinished", player.isRoundFinished());
            }

            infoMap_total.add(infoMap);
        }
        sendMessage("/plays/info/"+roomId, roomId, infoMap_total, receipId);
    }



    public void broadcastSpeakerAudio(String roomId, String userId, String voice) {
        HashMap<String, Object> info = new HashMap<>();
        info.put("userID", userId);
        info.put("audioData", voice);
        sendMessage("/plays/audio/"+roomId, roomId, info, null);
    }

    // public void broadcastAudio(String roomId, Map<String, String> VoiceDict) {
    //     HashMap<String, Object> info = new HashMap<>();
    //     info.put("listOfaudioData", VoiceDict);
    //     sendMessage("/plays/audio", roomId, info, null);
    // }

}
