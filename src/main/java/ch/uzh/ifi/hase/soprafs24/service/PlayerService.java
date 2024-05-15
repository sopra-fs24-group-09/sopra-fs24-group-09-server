package ch.uzh.ifi.hase.soprafs24.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;

import org.springframework.beans.factory.annotation.Qualifier;

import ch.uzh.ifi.hase.soprafs24.constant.PlayerStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class PlayerService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final PlayerRepository playerRepository;
    public PlayerService(@Qualifier("playerRepository") PlayerRepository playerRepository) {
      this.playerRepository = playerRepository;
    }


    public List<Player> getPlayers() {
        return this.playerRepository.findAll();
    }

    public Player createPlayer(Player newPlayer) {
    newPlayer.setToken(UUID.randomUUID().toString());
    newPlayer.setPlayerStatus(PlayerStatus.UNREADY);
    newPlayer = playerRepository.save(newPlayer);
    playerRepository.save(newPlayer);

    log.debug("Created Information for player: {}", newPlayer);
    return newPlayer;
    }

    public Player findPlayerById(String playerId){
        if (playerRepository.findById(playerId).isPresent()){
            if(playerRepository.findById(playerId).isEmpty()){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found");
            }
            return playerRepository.findById(playerId).get();
        }
        else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found");
        }
    }
}
