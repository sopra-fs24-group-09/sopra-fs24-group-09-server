package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

@Repository("userRepository")
public interface UserRepository extends JpaRepository<User, Long> {
  // as we only need the username now so note it
  // User findByName(String name);
  User getOne(@RequestParam(required = true) Long id);
  User findByUsername(String username);
}
