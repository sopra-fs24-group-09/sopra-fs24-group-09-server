package ch.uzh.ifi.hase.soprafs24.service;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import java.util.concurrent.*;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.constant.RoundStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;

import ch.uzh.ifi.hase.soprafs24.constant.PlayerStatus;
import ch.uzh.ifi.hase.soprafs24.constant.RoomProperty;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class GameService {

    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    private final UserService userService;
    private final SocketService socketService;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private CountDownLatch latch = new CountDownLatch(500000);

    public void executeWithTimeout(Runnable task, long timeout, TimeUnit unit) {
        Future<?> future = executor.submit(task);
    
        executor.schedule(() -> {
            if (!future.isDone()) {
                future.cancel(true);
            }
        }, timeout, unit);
    
        try {
            boolean completed = this.latch.await(timeout, unit);
            if (!completed) {
                // Handle the case where the wait timed out
                System.out.println("Task timed out");
            } else {
                // Handle the case where the wait completed
                System.out.println("Task completed successfully");
            }
        } catch (InterruptedException e) {
            // e.printStackTrace();
            // Handle the case where the wait was interrupted
            System.out.println("Task interrupted");
        }
        }

    public GameService(@Qualifier("playerRepository") PlayerRepository playerRepository,
            @Qualifier("userRepository") UserRepository userRepository,
            @Qualifier("gameRepository") GameRepository gameRepository,
            UserService userService, SocketService socketService,
            @Qualifier("roomRepository") RoomRepository roomRepository) {
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.userService = userService;
        this.socketService = socketService;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    public void Ready(String userId, String roomId) {
        User user = userService.findUserById(userId);
        // String jsonMessage = "{\"message\":\"This room is full!\"}"; 
        // template.convertAndSendToUser(user.getId(), "/response/"+ roomId, jsonMessage);
        user.setPlayerStatus(PlayerStatus.READY);
        userRepository.save(user);
    }

    public void UnReady(String userId) {
        User user = userService.findUserById(userId);

        user.setPlayerStatus(PlayerStatus.UNREADY);
        userRepository.save(user);
    }

    public void checkIfAllReady(Room room) {
        if (room.getRoomPlayersList().size() < 2) {
            throw new RuntimeException( "2 or more players are required to start the game");
        }

        //check if the game is start already
        if (room.getRoomProperty() == RoomProperty.INGAME) {
            throw new RuntimeException("The game has already been started for this room");
        }
        
        List<String> playerList = room.getRoomPlayersList();
        for (String id : playerList) { // Fix: Iterate over the playerList
            User user = userService.findUserById(id); // Fix: Get the user from the player
            if (user.getPlayerStatus() != PlayerStatus.READY) {
                throw new RuntimeException("Not all players are ready");
                // Notify unready players
            }
        }
    }

    @Async("taskExecutor")
    public void startGame(Room room) {
        // Initialize a new game object and player objects, then save them to the
        // database
        room.setRoomProperty(RoomProperty.INGAME);
        roomRepository.save(room);
        socketService.broadcastLobbyInfo();
        Game game = new Game(room);

        if (game.getTheme() == null) {
            throw new IllegalStateException("Game theme is not set");
            // or handle this case in a way that makes sense for your application
        }
        
        game.setGameStatus(GameStatus.ingame);
        gameRepository.save(game);
        List<String> words;
        // Assign a word to each player according to their index in the player list to
        words = room.getRoomWordsList();
        Collections.shuffle(words);

        for (String id : game.getRoomPlayersList()) {
            User user = userService.findUserById(id);
            Player player = new Player(user);
//            game.addPlayerList(player);
            // Assign a word to each player according to their index in the player list to
            // avoid the same word being assigned to multiple players, and the shuffle
            // before makes sure it's random every time
            player.setAssignedWord(words.get(game.getRoomPlayersList().indexOf(player.getId())));
            playerRepository.save(player);
            gameRepository.save(game);
        }

        // socketService.broadcastGameinfo(game.getRoomId(), "speak");
        // socketService.broadcastPlayerInfo(game.getRoomId(), "123", "speak");

        // Proceed through each turn of the game until every player has spoken

        while (game.getCurrentRoundNum() < game.getRoomPlayersList().size()) {
            String currentId = game.getRoomPlayersList().get(game.getCurrentRoundNum());
            Player currentSpeaker = playerRepository.findById(currentId).get();
            currentSpeaker.setIfGuessed(false);
            playerRepository.save(currentSpeaker);

            game.setCurrentSpeaker(currentSpeaker);
            gameRepository.save(game);

            socketService.broadcastGameinfo(game.getRoomId(), "speak");
            socketService.broadcastPlayerInfo(game.getRoomId(), "speak");
            proceedTurn(game);
            jumpToNextRound(game);
        }

        // Display scores after every player has spoken
        System.out.println("Revealing："+LocalDateTime.now());
//        Runnable displayScoresTask = () -> displayScores(game);
//        executeWithTimeout(displayScoresTask, 50, TimeUnit.SECONDS);
        displayScores(game);
        room.setRoomProperty(RoomProperty.GAMEOVER);
        roomRepository.save(room);
        socketService.broadcastGameinfo(game.getRoomId(), "gameover");
        socketService.broadcastPlayerInfo(game.getRoomId(), "gameover");
        socketService.broadcastLobbyInfo();
//        try {
//            Thread.sleep(30000);//20000
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new RuntimeException(e);
//        }
//        System.out.println("End Revealing："+ LocalDateTime.now());
        // Display the leaderboard for 2 minutes, and dismiss the room in advance if all
        // players leave
        if (gameRepository.findByRoomId(game.getRoomId()).isPresent()){
            endGame(game);
        }
    }

    public void proceedTurn(Game game) {
        // Speak
        game.setRoundStatus(RoundStatus.speak);
        gameRepository.save(game);

        System.out.println("Speak："+LocalDateTime.now());
//        Runnable speakPhaseTask = () -> speakPhase(game);
//        executeWithTimeout(speakPhaseTask, 20, TimeUnit.SECONDS);
        speakPhase(game);
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        System.out.println("End speak"+ LocalDateTime.now());

        String voice = playerRepository.findById(game.getCurrentSpeaker().getId()).get().getAudioData();
        
        // Guess - if no audio uploaded, jump to next round
        if (voice != null &&  voice.length() != 0) {
            Player currentSpeaker = playerRepository.findById(game.getCurrentSpeaker().getId()).get();
            currentSpeaker.setRoundFinished(true);
            playerRepository.save(currentSpeaker);
            socketService.broadcastSpeakerAudio(game.getRoomId(), game.getCurrentSpeaker().getId(),voice);
            game.setRoundStatus(RoundStatus.guess);
            gameRepository.save(game);

            System.out.println("guess："+LocalDateTime.now());
//            Runnable guessPhaseTask = () -> guessPhase(game);
//            executeWithTimeout(guessPhaseTask, 30, TimeUnit.SECONDS);

            guessPhase(game);

            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            System.out.println("endguess："+LocalDateTime.now());

        } else {
            // if the speaker does not upload the audio, he will get -4 points and marked this word as No Speak
            if(playerRepository.findById(game.getCurrentSpeaker().getId()).isEmpty()){
                throw new RuntimeException("Player not found");
            }
            Player speaker = playerRepository.findById(game.getCurrentSpeaker().getId()).get();
            speaker.setSpeakScore(speaker.getSpeakScore() - 4);
            playerRepository.save(speaker);
        }

        calculateScore(game);
        revealPhase(game);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public void calculateScore(Game game_old) {
        Game game = gameRepository.findByRoomId(game_old.getRoomId()).get();
        for (String playerId : game.getRoomPlayersList()) {
            Player player = playerRepository.findById(playerId).get();
            // Speaker or Guesser
            if (!player.getId().equals(game.getCurrentSpeaker().getId())) {
                // Did not guess or wrong answer
                if (!player.isRoundFinished()) {
                    player.addScoreDetail(game.getCurrentAnswer(), 0, 0);
                    playerRepository.save(player);
                }
                // Correct answer
                else {
                    Integer score = game.getRoomPlayersList().size() - game.getAnsweredPlayerList().indexOf(player.getId());
                    player.setGuessScore(player.getGuessScore() + score);
                    player.addScoreDetail(game.getCurrentAnswer(), 0, score);
                    playerRepository.save(player);
                }
            }
            else {
                // Speaker gets 2 points for each guesser
                Integer scoreList = gameRepository.findByRoomId(game.getRoomId()).get().getAnsweredPlayerList().size();
                Integer score = 2*scoreList;

                player.setSpeakScore(player.getSpeakScore() + score);
                player.addScoreDetail(game.getCurrentAnswer(), 1, player.getSpeakScore());
                playerRepository.save(player);
            }
            player.setTotalScore(player.getSpeakScore() + player.getGuessScore());
            playerRepository.save(player);
        }
    }

    public void jumpToNextRound(Game game) {
        for (String playerId : game.getRoomPlayersList()) {
            Player player = playerRepository.findById(playerId).get();
            player.setAudioData("");
            player.setRoundFinished(false);
            player.setIfGuessed(true);
            playerRepository.save(player);
        }

        // Clear the audio data of all players ??

        game.setCurrentRoundNum(game.getCurrentRoundNum() + 1);
        game.getAnsweredPlayerList().clear();
        gameRepository.save(game);
    }

    public void speakPhase(Game game) {
        // Give a word with API
        game.setCurrentAnswer(game.getCurrentSpeaker().getAssignedWord());
        game.setRoundDue(String.valueOf(ZonedDateTime.now(ZoneId.of("UTC")).plusSeconds(20)));
        gameRepository.save(game);
        socketService.broadcastGameinfo(game.getRoomId(), "speak");
        socketService.broadcastPlayerInfo(game.getRoomId(), "speak");
        // Wait for the player to upload audio -- In playerService
//        CountDownLatch latch = new CountDownLatch(2);
    }

    public void guessPhase(Game game) {
        game.setRoundDue(String.valueOf(ZonedDateTime.now(ZoneId.of("UTC")).plusSeconds(60)));
        gameRepository.save(game);
        socketService.broadcastGameinfo(game.getRoomId(), "guess");
        socketService.broadcastPlayerInfo(game.getRoomId(),  "guess");
//        latch = new CountDownLatch(game.getRoomPlayersList().size() - 1);
    }

    public void revealPhase(Game game) {
        game.setRoundStatus(RoundStatus.reveal);
        game.setRoundDue(String.valueOf(ZonedDateTime.now(ZoneId.of("UTC")).plusSeconds(10)));
        gameRepository.save(game);
        socketService.broadcastGameinfo(game.getRoomId(), "reveal");
        socketService.broadcastPlayerInfo(game.getRoomId(), "null");
    }

    public void endGame(Game game) {
        for (String userId : roomRepository.findByRoomId(game.getRoomId()).get().getRoomPlayersList()) {
            User user = userRepository.findById(userId).get();
            if (user.getInRoomId().equals(game.getRoomId())) {
                if (playerRepository.findById(userId).isPresent()) {
                    Player player = playerRepository.findById(userId).get();
                    playerRepository.delete(player);
                }
                user.setPlayerStatus(PlayerStatus.UNREADY);
                user.setInRoomId(null);
                userRepository.save(user);
            }
        }
        gameRepository.delete(game);

        System.out.println("Game ended");
        if (roomRepository.findByRoomId(game.getRoomId()).isPresent()){
            roomRepository.delete(roomRepository.findByRoomId(game.getRoomId()).get());
            socketService.broadcastLobbyInfo();
        }
    }

    public void validateAnswer(Game game, Player player, String guess) {
        if (guess.equals(game.getCurrentAnswer())) {

            player.setRoundFinished(true);
            playerRepository.save(player);

            game.getAnsweredPlayerList().add(player.getId());
            gameRepository.save(game);

            socketService.broadcastGameinfo(game.getRoomId(), "score");
            socketService.broadcastPlayerInfo(game.getRoomId(), "null");
//            latch.countDown();
        } else {
            throw new RuntimeException("Wrong answer");
        } 
    }

    public void displayRoundScores(Player player) {
        // Display the scores of all players for this round
        player.getScoreDetails();
    }

    public void displayScores(Game game) {
        // Display the scores of all players in the game, data required by Yixuan
        game.setRoundStatus(RoundStatus.reveal);
        game.setGameStatus(GameStatus.over);
        gameRepository.save(game);
        socketService.broadcastGameinfo(game.getRoomId(), "score");
        socketService.broadcastPlayerInfo(game.getRoomId(), null);
    }

    public Game findGameById(String roomId) {
        if (gameRepository.findById(roomId).isPresent()) {
            return gameRepository.findById(roomId).get();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
    }

    public void setPlayerAudio(String roomId, String playerId, String voice) {
        Game game = gameRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + roomId));

        List<Player> playerlist = new ArrayList<>();
        for (String id : game.getRoomPlayersList()) {
            Player player = playerRepository.findById(id).get();
            playerlist.add(player);
        }
        Player player = playerRepository.findById(playerId).get();
        boolean isPlayerInList = playerlist.stream().anyMatch(p -> p.getId().equals(player.getId()));
        if (isPlayerInList) {
            player.setAudioData(voice);
            playerRepository.save(player);
//            latch.countDown();

            if (game.getRoundStatus() == RoundStatus.guess) {
                socketService.broadcastSpeakerAudio(game.getRoomId(), player.getId(), voice);
            }

        } else {
            throw new IllegalArgumentException("Player not found in the game with ID: " + playerId);
        }
        socketService.broadcastPlayerInfo(roomId, "audio");
    }

    public void setURL(URL mockUrl) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setURL'");
    }
}
