package ch.uzh.ifi.hase.soprafs24.repository;
//import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import  org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository("gameRepository")
public interface GameRepository extends MongoRepository<Game, String> {
    Optional<Game> findByRoomId(String roomId);

    @Override
    List<Game> findAll();
}

