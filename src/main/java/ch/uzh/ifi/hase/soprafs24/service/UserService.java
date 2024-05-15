package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

  private Random random;

  public static final String[] AVATARS = {
      "angry-face",
      "angry-face-with-horns",
      "anguished-face",
      "anxious-face-with-sweat",
      "astonished-face",
      "beaming-face-with-smiling-eyes",
      "cat-face",
      "clown-face",
      "cold-face",
      "confounded-face",
      "confused-face",
      "cow-face",
      "cowboy-hat-face",
      "crying-face",
      "disappointed-face",
      "disguised-face",
      "dog-face",
      "dotted-line-face",
      "downcast-face-with-sweat",
      "dragon-face",
      "drooling-face",
      "expressionless-face",
      "face-blowing-a-kiss",
      "face-exhaling",
      "face-holding-back-tears",
      "face-in-clouds",
      "face-savoring-food",
      "face-screaming-in-fear",
      "face-vomiting",
      "face-with-crossed-out-eyes",
      "face-with-diagonal-mouth",
      "face-with-hand-over-mouth",
      "face-with-head-bandage",
      "face-with-medical-mask",
      "face-with-monocle",
      "face-with-open-eyes-and-hand-over-mouth",
      "face-with-open-mouth",
      "face-with-peeking-eye",
      "face-with-raised-eyebrow",
      "face-with-rolling-eyes",
      "face-with-spiral-eyes",
      "face-with-steam-from-nose",
      "face-with-symbols-on-mouth",
      "face-with-tears-of-joy",
      "face-with-thermometer",
      "face-with-tongue",
      "face-without-mouth",
      "fearful-face",
      "first-quarter-moon-face",
      "flushed-face"
  };

  public UserService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
    this.random = new Random();
  }

  public String getRandomAvatar() {
    int idx = random.nextInt(AVATARS.length);
    return AVATARS[idx];
  }

  public List<User> getUsers() {
    return this.userRepository.findAll();
  }

  public User createUser(User newUser) {
    // check if the username with spaces or special characters
    if (newUser.getUsername() == null || newUser.getUsername().isEmpty()
        || newUser.getUsername().matches(".*[\\s\\p{Punct}].*")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "User name must not be empty and should not contain spaces or special characters");
    }
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.OFFLINE);
    newUser.setRegisterDate(new Date());
    newUser.setAvatar(getRandomAvatar());
    checkIfUserExists(newUser);
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    newUser.setStatus(UserStatus.ONLINE);
    newUser = userRepository.save(newUser);
    userRepository.save(newUser);

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

  /**
   * This is a helper method that will check the uniqueness criteria of the
   * username and the name
   * defined in the User entity. The method will do nothing if the input is unique
   * and throw an error otherwise.
   *
   * @param userToBeCreated
   * @throws org.springframework.web.server.ResponseStatusException
   * @see User
   */
  private void checkIfUserExists(User userToBeCreated) {
    String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
    if (userRepository.findByUsername(userToBeCreated.getUsername()).isPresent()) {
      String errorMessage = String.format(baseErrorMessage, "username", "is");
      throw new ResponseStatusException(HttpStatus.CONFLICT, errorMessage);
    }
  }

  public User loginUser(User user) {
    Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
    if (!existingUser.isPresent()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with the provided username.");
    }
    user = checkIfPasswordWrong(user);
    user.setStatus(UserStatus.ONLINE);
    user.setToken(UUID.randomUUID().toString());
    userRepository.save(user);
    return user;
  }

  User checkIfPasswordWrong(User userToBeLoggedIn) {
    if(userRepository.findByUsername(userToBeLoggedIn.getUsername()).isEmpty()){
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Username not exist!");
    }
    User userByUsername = userRepository.findByUsername(userToBeLoggedIn.getUsername()).get();

    if (userByUsername == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Username not exist!");
    } else if (!userByUsername.getPassword().equals(userToBeLoggedIn.getPassword())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Password incorrect!");
    } else {
      return userByUsername;
    }
  }

  // Define the logout function to set the status to OFFLINE when log out
  public User logoutUser(User userTobeLoggedOut) {
    Optional<User> userOptional = userRepository.findById(userTobeLoggedOut.getId());
    if (!userOptional.isPresent()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }

    User user = userOptional.get();
    if (user.getStatus() == UserStatus.OFFLINE) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already logged out");
    }

    try {
      user.setStatus(UserStatus.OFFLINE);
      userRepository.save(user);
      return null;
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during logout", e);
    }
  }

  public User userProfileById(String id) {
    Optional<User> userByUserid = userRepository.findById(id);
    if (userByUserid.isPresent()) {
      if (userByUserid.isEmpty()) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
      }
      return userByUserid.get();
    } else {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with this ID:" + id + " not found!");
    }
  }

  public void userEditProfile(String id, User user) {
    if (!userRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user ID was not found");
    }
    User userByUserid = userRepository.findById(id).get();

    if (user.getUsername() != null) {
      checkIfUserExists(user);
      userByUserid.setUsername(user.getUsername());
    }
    ;
    // set the birthday
    if (user.getAvatar() != null) {
      userByUserid.setAvatar(user.getAvatar());
    }
    ;

    userRepository.save(userByUserid);
  }

  public User findUserById(String userId) {
    if (userRepository.findById(userId).isPresent()) {
      if(userRepository.findById(userId).isEmpty()){
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
      }
      return userRepository.findById(userId).get();
    } else {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }
  }

  public boolean findByToken(String token) {
    if (userRepository.findByToken(token) == null) {
      return false;
    }
    return true;
  }
}
