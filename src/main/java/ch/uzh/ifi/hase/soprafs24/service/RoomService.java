package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.PlayerStatus;
import ch.uzh.ifi.hase.soprafs24.constant.RoomProperty;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class RoomService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;

    public RoomService(@Qualifier("roomRepository") RoomRepository roomRepository,
            @Qualifier("userRepository") UserRepository userRepository,
            @Qualifier("gameRepository") GameRepository gameRepository,
            @Qualifier("playerRepository") PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
    }

    public List<Room> getRooms() {
        return this.roomRepository.findAll();
    }

    public boolean checkRoomExists(String roomId) {
        return roomRepository.findByRoomId(roomId).isPresent();
    }

    // Here we create a new room, and we need to set the room property and theme
    // according to the input from client
    public Room createRoom(Room newRoom) {
        Optional<Room> existingRoom = roomRepository.findByRoomId(newRoom.getRoomId());
        if (existingRoom.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room already exists");
        }

        // check if the room name is empty
        if (newRoom.getRoomName() == null || newRoom.getRoomName().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room name must not be empty");
        }

        // check if the room name contains spaces
        if (newRoom.getRoomName().chars().anyMatch(Character::isWhitespace)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room name should not contain spaces");
        }

        // check if the room name contains special characters
        if (newRoom.getRoomName().chars().anyMatch(ch -> !Character.isLetterOrDigit(ch))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Room name should not contain special characters");
        }

        // check if the number of players is between 2 and 5
        if (newRoom.getMaxPlayersNum() < 2 || newRoom.getMaxPlayersNum() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The number of players should be between 2 and 5");
        }

        try {
            newRoom.setRoomName(newRoom.getRoomName());
            newRoom.setTheme(newRoom.getTheme());
            newRoom.setMaxPlayersNum(newRoom.getMaxPlayersNum());
            newRoom.setRoomOwnerId(newRoom.getRoomOwnerId());
            newRoom.setRoomProperty(RoomProperty.WAITING);
            newRoom.addRoomPlayerList(newRoom.getRoomOwnerId());
            newRoom.setRoomPlayersList(newRoom.getRoomPlayersList());

            final String theme = newRoom.getTheme().toString();
            // Async fetch words for the theme while creating the room
            CompletableFuture<List<String>> wordsFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return getWords(theme);
                } catch (IOException e) {
                    System.err.println("Error while getting words: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            });

            // Save the room to enter but loading words before start
            newRoom = roomRepository.save(newRoom);

            User roomOwner = userRepository.findById(newRoom.getRoomOwnerId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            roomOwner.setPlayerStatus(PlayerStatus.READY);
            roomOwner.setInRoomId(newRoom.getRoomId());
            userRepository.save(roomOwner);

            String roomId = newRoom.getRoomId();
            wordsFuture.thenAccept(wordsList -> {
                Optional<Room> optionalRoom = roomRepository.findByRoomId(roomId);
                if (optionalRoom.isPresent()) {
                    Room roomToUpdate = optionalRoom.get();
                    roomToUpdate.setRoomWordsList(wordsList);
                    roomRepository.save(roomToUpdate);
                } else {
                    System.err.println("Room not found for id: " + roomId);
                }
            });

            log.debug("Created Information for Room: {}", newRoom);
            return newRoom;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Something unexpected went wrong when creating a game", e);
        }
    }

    public List<String> getWords(String theme) throws IOException {
        List<String> words = new ArrayList<>();
        String apiUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
        String apiKey = System.getenv("API_KEY");

        // Build request with glm-4 model and a message asking for a list of words with
        // the given theme
        String requestBody = "{\n" +
                "    \"model\": \"glm-4\",\n" +
                "    \"messages\": [\n" +
                "        {\n" +
                "            \"role\": \"user\",\n" +
                "            \"content\": \"Generate a JSON list of noun words with the theme '" + theme
                + "' and ensure each word has no more than four syllables and all in lowercase in the following format: {\\\"words\\\": [\\\"word1\\\", \\\"word2\\\", \\\"word3\\\", ...]}\"\n"
                +
                "        }\n" +
                "    ]\n" +
                "}";

        // Connection
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Send request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Get response
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                // Resolve JSON response
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(response.toString());
                JsonNode wordsNode = jsonResponse.path("choices").get(0).path("message").path("content");

                // Extract JSON from message content
                String messageContent = wordsNode.asText();
                Pattern pattern = Pattern.compile("\\{\\s*\"words\"\\s*:\\s*\\[[^\\]]*\\]\\s*\\}");
                Matcher matcher = pattern.matcher(messageContent);
                if (matcher.find()) {
                    String jsonString = matcher.group();

                    // Wordlist
                    JsonNode wordsList = objectMapper.readTree(jsonString).path("words");

                    for (JsonNode wordNode : wordsList) {
                        String word = wordNode.asText();
                        if (Pattern.matches("^[a-zA-Z]+$", word)) {
                            words.add(word);
                        }
                    }
                } else {
                    throw new IOException("Failed to extract JSON from message content");
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException("Failed to parse JSON response", e);
            }
        } else {
            throw new IOException("HTTP response code: " + responseCode);
        }

        // Lowercase all words
        for (int i = 0; i < words.size(); i++) {
            words.set(i, words.get(i).toLowerCase());
        }
        
        return words;
    }

    // public Room findRoomById(String userId, String roomId){
    // if (!roomRepository.findByRoomId(roomId).isPresent()){
    // // String jsonMessage = "{\"message\":\"Room not found!\"}";
    // // template.convertAndSendToUser(userId, "/response/"+ roomId, jsonMessage);
    // throw new RuntimeException( "Room not found");
    // }
    // return roomRepository.findByRoomId(roomId).get();
    // }

    public <T> void enterRoom(Room room, User user) {
        // if (room.getRoomPlayersList().contains(user.getId())) {
        // throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already in
        // game");
        // }
        // Check full or not
        if (room.getRoomPlayersList().size() >= room.getMaxPlayersNum()) {
            // String jsonMessage = "{\"message\":\"This room is full!\"}";
            // template.convertAndSendToUser(user.getId(), "/response/"+ room.getRoomId(),
            // jsonMessage);
            throw new RuntimeException("This room is full!");
        }
        if (room.getRoomProperty() != RoomProperty.WAITING) {
            // String jsonMessage = "{\"message\":\"You can not enter a room that is in
            // game!\"}";
            // template.convertAndSendToUser(user.getId(), "/response/"+ room.getRoomId(),
            // jsonMessage);
            throw new RuntimeException("You can not enter a room that is in game!");
        }
        if (user.getInRoomId() != null && !user.getInRoomId().equals(room.getRoomId())) {
            if (!roomRepository.findByRoomId(user.getInRoomId()).isPresent()) {
                user.setInRoomId(null);
            } else {
                throw new RuntimeException("You can not enter a room when you are in another room!");
            }
        }
        boolean id_equal = (user.getId()).equals(room.getRoomOwnerId());

        // if the user is not room owner then set the status to unready
        if (!id_equal) {
            user.setPlayerStatus(PlayerStatus.UNREADY);
            user.setInRoomId(room.getRoomId());
        } else {
            user.setPlayerStatus(PlayerStatus.READY);
            user.setInRoomId(room.getRoomId());
        }
        room.addRoomPlayerList(user.getId());
        System.out.println("Roomplayerslist now is:" + room.getRoomPlayersList());
        userRepository.save(user);
        roomRepository.save(room);
    }

    public void exitRoom(Room room, User user) {
        if (!room.getRoomPlayersList().contains(user.getId())) {
            throw new RuntimeException("User is not in game");
        }
        if (room.getRoomOwnerId().equals(user.getId()) && room.getRoomPlayersList().size() == 1) {
            if (gameRepository.findByRoomId(room.getRoomId()).isPresent()) {
                System.out.println(room.getRoomId());
                System.out.println(gameRepository.findByRoomId(room.getRoomId()).isPresent());
                gameRepository.delete(gameRepository.findByRoomId(room.getRoomId()).get());
            }
            roomRepository.delete(room);
        } else {
            if (room.getRoomOwnerId().equals(user.getId()) && room.getRoomPlayersList().size() > 1) {
                room.setRoomOwnerId(room.getRoomPlayersList().get(1));
                User newOwner = userRepository.findById(room.getRoomPlayersList().get(1)).get();
                newOwner.setPlayerStatus(PlayerStatus.READY);
                userRepository.save(newOwner);
            }
            room.getRoomPlayersList().remove(user.getId());
            roomRepository.save(room);
        }
        if (playerRepository.findById(user.getId()).isPresent()) {
            playerRepository.delete(playerRepository.findById(user.getId()).get());
        }
        user.setPlayerStatus(PlayerStatus.UNREADY);
        user.setInRoomId(null);
        userRepository.save(user);
    }

    /**
     * This is a helper method that will check the uniqueness criteria of the
     * username and the name
     * defined in the User entity. The method will do nothing if the input is unique
     * and throw an error otherwise.
     *
     * @param userToBeCreated
     * @throws org.springframework.web.server.ResponseStatusException
     * @see User
     */

}
