package ch.uzh.ifi.hase.soprafs24.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import ch.uzh.ifi.hase.soprafs24.constant.RoundStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;

import ch.uzh.ifi.hase.soprafs24.constant.PlayerStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class GameService {

    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    private final PlayerService playerService;
    private final UserService userService;
    private final SocketService socketService;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private CountDownLatch latch;

    public void executeWithTimeout(Runnable task, long timeout, TimeUnit unit) {
        Future<?> future = executor.submit(task);

        executor.schedule(() -> {
            if (!future.isDone()) {
                future.cancel(true);
            }
        }, timeout, unit);

        try {
            latch.await(timeout, unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public GameService(@Qualifier("playerRepository") PlayerRepository playerRepository, @Qualifier("userRepository") UserRepository userRepository, @Qualifier("gameRepository") GameRepository gameRepository, PlayerService playerService, UserService userService, SocketService socketService, @Qualifier("roomRepository") RoomRepository roomRepository) {
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.playerService = playerService;
        this.userService = userService;
        this.socketService=socketService;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    public void Ready(String userId) {
        User user = userService.findUserById(userId);

        user.setPlayerStatus(PlayerStatus.READY);
        userRepository.save(user);
    }

    public void UnReady(String userId){
        User user = userService.findUserById(userId);

        user.setPlayerStatus(PlayerStatus.UNREADY);
        userRepository.save(user);
    }

    public void checkIfAllReady(Room room){
        List<String> playerList = room.getRoomPlayersList();
        for (String id : playerList){
            User user = userService.findUserById(id);
            if (user.getPlayerStatus() != PlayerStatus.READY){
                // Notify unready players
            }
        }
        startGame(room);
    }

    public void startGame(Room room){
        // Create a new game object and player objects then save them to the database
        Game game = new Game(room);
        socketService.broadcastGamestart(room.getRoomId());
        gameRepository.save(game);
        for (String id : game.getRoomPlayersList()) {
            User user = userService.findUserById(id);
            Player player = new Player(user);
            game.getPlayerList().add(player);
            player.setAssignedWord("TestWord"); //TODO: get word from API
            playerRepository.save(player);
            gameRepository.save(game);
        }
        while (game.getCurrentRoundNum()<game.getRoomPlayersList().size()){
            Player currentSpeaker = game.getPlayerList().get(game.getCurrentRoundNum());
            game.setCurrentSpeaker(currentSpeaker);
            gameRepository.save(game);
            proceedTurn(game);
        }
        displayScores(game);
    }

    public void proceedTurn(Game game){
        // Speak
        Runnable speakPhaseTask = () -> speakPhase(game);
        executeWithTimeout(speakPhaseTask, 30, TimeUnit.SECONDS);

        // Guess - if no audio uploaded, jump to next round
        if (!game.getCurrentSpeaker().getAudioData().equals("") && game.getCurrentSpeaker().getAudioData()!=null){
            Runnable guessPhaseTask = () -> guessPhase(game);
            executeWithTimeout(guessPhaseTask, 60, TimeUnit.SECONDS);
        }
        else {
            // if the speaker does not upload the audio, he will get -4 points and marked this word as No Speak
            for (Player player : game.getPlayerList()) {
                if (player != game.getCurrentSpeaker()) {
                    player.addScoreDetail("No Speak", 0, 0);
                    playerRepository.save(player);
                }
                else {
                    player.setSpeakScore(player.getSpeakScore()-4);
                    playerRepository.save(player);
                }
            }
        }

        //Intermediate display
        game.getCurrentSpeaker().addScoreDetail(game.getCurrentAnswer(), 1, game.getCurrentSpeaker().getSpeakScore());
        playerRepository.save(game.getCurrentSpeaker());
        // displayRoundScores(game);

        // Prepare for Next Round
        jumpToNextRound(game);
    }

    public void jumpToNextRound(Game game){
        game.getCurrentSpeaker().setIfGuessed(true);
        playerRepository.save(game.getCurrentSpeaker());
        // Clear the audio data of all players ??

        game.setCurrentRoundNum(game.getCurrentRoundNum()+1);
        game.getAnsweredPlayerList().clear();
        gameRepository.save(game);

    }

    public void speakPhase(Game game){
        game.setRoundStatus(RoundStatus.SPEAKING);
        // Give a word with API
        game.setCurrentAnswer(game.getCurrentSpeaker().getAssignedWord());
        gameRepository.save(game);
        // Wait for the player to upload audio -- In playerService
        latch = new CountDownLatch(1);
    }

    public void guessPhase(Game game){
        game.setRoundStatus(RoundStatus.GUESSING);
        gameRepository.save(game);
        latch = new CountDownLatch(game.getRoomPlayersList().size()-1);
    }

    // For speaker to upload audio
    public void speakerUpload(Game game, String audioData){
        game.getCurrentSpeaker().setAudioData(audioData);
        playerRepository.save(game.getCurrentSpeaker());
        //上传成功，发给所有玩家
        socketService.broadcastSpeakerAudio(game.getRoomId(),game.getCurrentSpeaker().getId(), game.getCurrentSpeaker().getAudioData());
        latch.countDown();
    }

    // For guesser to upload audio to share
    public void gusserUpload(Player player, String audioData){
        player.setAudioData(audioData);
        playerRepository.save(player);
        //上传成功，发给所有玩家
    }

    public void endGame(Game game){
        gameRepository.delete(game);
        roomRepository.delete(roomRepository.findById(game.getRoomId()).get());
    }

    public void validateAnswer(Game game, Player player, String answer){
        if (answer.equals(game.getCurrentAnswer())){
            // Add score for gussers depending on the answer speed
            int score = game.getPlayerList().size()-game.getAnsweredPlayerList().size();
            player.setGuessScore(player.getGuessScore()+ score);
            player.addScoreDetail(answer, 0, score);

            // Fixed score for speaker for each correct answer
            game.getCurrentSpeaker().setSpeakScore(game.getCurrentSpeaker().getSpeakScore()+2);

            game.getAnsweredPlayerList().add(player);
            gameRepository.save(game);
            playerRepository.save(player);
            playerRepository.save(game.getCurrentSpeaker());
            latch.countDown();
            //显示回复正确加分
        }
        else {}//显示回复错误
    }

    public void displayRoundScores(Player player){
        // Display the scores of all players for this round
        player.getScoreDetails();
        // 展示回合得分
    }

    public void displayScores(Game game){
        // Display the scores of all players in the game, data required by Yixuan
    }

    // 从结果展示界面离开房间，删除玩家
    public void leaveRoom(Game game, Player player){
        game.getPlayerList().remove(player);
        gameRepository.save(game);
        playerRepository.delete(player);
        if (game.getPlayerList().size() == 0){endGame(game);}
    }


    public Game findGameById(String roomId){
        if (gameRepository.findById(roomId).isPresent()){
            return gameRepository.findById(roomId).get();
        }
        else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
    }

    // May cause loop usage

    // public Player findPlayerInGame(String playerId, String roomId){
    //     Game game = gameRepository.findById(roomId)
    //     .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + roomId));
    //     List<Player> playerlist = game.getPlayerList();
    //     Player player = playerService.findPlayerById(playerId);
    //     boolean isPlayerInList = playerlist.stream().anyMatch(p -> p.getId().equals(player.getId()));
    //     if(isPlayerInList){
    //         return player;
    //     }
    //     else{
    //         return null;
    //     }
    // }

    public void setPlayerAudio(String roomId, String playerId, String voice){
        Game game = gameRepository.findById(roomId)
        .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + roomId));
        List<Player> playerlist = game.getPlayerList();
        Player player = playerService.findPlayerById(playerId);
        boolean isPlayerInList = playerlist.stream().anyMatch(p -> p.getId().equals(player.getId()));
        if(isPlayerInList){
            player.setAudioData(voice);
        }
        else{
            throw new IllegalArgumentException("Player not found in the game with ID: " + playerId);
        }
    }

    public String getPlayerAudio(String roomId, String playerId ){
        Game game = gameRepository.findById(roomId)
        .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + roomId));
        List<Player> playerlist = game.getPlayerList();
        Player player = playerService.findPlayerById(playerId);
        boolean isPlayerInList = playerlist.stream().anyMatch(p -> p.getId().equals(player.getId()));
        if(isPlayerInList){
            return player.getAudioData();
        }
        else{
            throw new IllegalArgumentException("Player not found in the game with ID: " + playerId);
        }
    }

    public Map<String, String> getAllPlayerAudio(String roomId) {
        Game game = gameRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + roomId));
        List<Player> playerlist = game.getPlayerList();
        Map<String, String> playerAudioMap = new HashMap<>();
    
        // Iterate over each player in the player list and add their ID and audio data to the map
        for (Player player : playerlist) {
            if (player.getAudioData() != null && !player.getAudioData().isEmpty()) {
                playerAudioMap.put(player.getId(), player.getAudioData());
            }
        }
    
        return playerAudioMap;
    }
}
