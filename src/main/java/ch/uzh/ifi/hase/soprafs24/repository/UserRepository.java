package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("userRepository")
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    User findByToken(String token);
    Optional<User> findById(String userId);

}