package ch.uzh.ifi.hase.soprafs24.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;

import ch.uzh.ifi.hase.soprafs24.constant.PlayerStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;

public class GameService {

    private PlayerRepository playerRepository;
    private GameRepository gameRepository;
    private PlayerService playerService;
    public GameService(@Qualifier("playerRepository") PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public void Ready(Long userId) {
        Optional<Player> optionalPlayer = playerRepository.findById(userId);
        
        optionalPlayer.ifPresent(player -> {
            player.setPlayerStatus(PlayerStatus.READY);
            playerRepository.save(player);
        });
        optionalPlayer.orElseThrow(() -> new RuntimeException("Player not found with id: " + userId));
    }

    public void UnReady(Long userId){
        Optional<Player> optionalPlayer = playerRepository.findById(userId);
        
        optionalPlayer.ifPresent(player -> {
            player.setPlayerStatus(PlayerStatus.UNREADY);
            playerRepository.save(player);
        });
        optionalPlayer.orElseThrow(() -> new RuntimeException("Player not found with id: " + userId));
    }

    public void startGame(Room room){
    }

    public Game findGameById(String roomId){
        return gameRepository.findById(roomId).get();
    }

    public Player findPlayerInGame(String playerId, String roomId){
        Game game = gameRepository.findById(roomId)
        .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + roomId));
        List<Player> playerlist = game.getPlayerList();
        Player player = playerService.findPlayerById(playerId);
        boolean isPlayerInList = playerlist.stream().anyMatch(p -> p.getId().equals(player.getId()));
        if(isPlayerInList){
            return player;
        }
        else{
            return null;
        }
    
    }
    
    public void validateAnswer(String answer){
        
    }

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
