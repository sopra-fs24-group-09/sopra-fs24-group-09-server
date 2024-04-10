package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("gameRepository")
public interface GameRepository extends MongoRepository<Game, String> {
    Optional<Game> findByGameId(String gameId);
}