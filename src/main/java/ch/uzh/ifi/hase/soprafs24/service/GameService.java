package ch.uzh.ifi.hase.soprafs24.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;

import ch.uzh.ifi.hase.soprafs24.constant.PlayerStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;

public class GameService {

    private final PlayerRepository playerRepository;
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
    
}
