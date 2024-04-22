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

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

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
  public UserService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> getUsers() {
    return this.userRepository.findAll();
  }

  public User createUser(User newUser) {
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.OFFLINE);
    newUser.setRegisterDate(new Date());
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

    User userByUsername = userRepository.findByUsername(userToBeLoggedIn.getUsername()).get();

    if (userByUsername == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Username not exist!");
    }
    else if (!userByUsername.getPassword().equals(userToBeLoggedIn.getPassword())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Password incorrect!");
    }
    else {
        return userByUsername;
    }
  }

  //Define the logout function to set the status to OFFLINE when log out
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
      return userByUserid.get();
    }
    else {throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with this ID:"+id+" not found!");}
  }

  public void userEditProfile(User user) {
    if(!userRepository.existsById(user.getId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user ID was not found");
      }
    User userByUserid = userRepository.findById(user.getId()).get();

    if(user.getUsername()!=null){
          checkIfUserExists(user);
          userByUserid.setUsername(user.getUsername());
          };
    // set the birthday
    if(user.getBirthday()!=null){
          userByUserid.setBirthday(user.getBirthday());
          };

    userRepository.save(userByUserid);
  }


    public User findUserById(String userId){
        if (userRepository.findById(userId).isPresent()){
            return userRepository.findById(userId).get();
        }
        else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }
}
