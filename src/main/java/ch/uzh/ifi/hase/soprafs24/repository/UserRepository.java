package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("userRepository")
public interface UserRepository extends MongoRepository<User, String> { // 注意这里的改变，假设User的ID是String类型
    Optional<User> findByUsername(String username);
    Optional<User> findById(String userId);

}