package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Player;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("playerRepository")
public interface PlayerRepository extends MongoRepository<Player, String> {
    Optional<Player> findByUsername(String username);
    Optional<Player> findById(Long userId);
    // You can add more query methods specific to the Player entity
}
