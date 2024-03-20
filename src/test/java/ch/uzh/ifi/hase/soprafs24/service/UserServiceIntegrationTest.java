package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@WebAppConfiguration
@SpringBootTest
public class UserServiceIntegrationTest {

  @Qualifier("userRepository")
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserService userService;

  @BeforeEach
  public void setup() {
    userRepository.deleteAll();
  }

  @Test
  public void createUser_validInputs_success() {
    // given
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");

    // when
    User createdUser = userService.createUser(testUser);

    // then
    assertEquals(testUser.getId(), createdUser.getId());
    assertEquals(testUser.getUsername(), createdUser.getUsername());
    assertEquals(testUser.getPassword(), createdUser.getPassword());
    assertNotNull(createdUser.getToken());
    assertEquals(UserStatus.ONLINE, createdUser.getStatus());
  }

  @Test
  public void createUser_duplicateUsername_throwsException() {
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");
    userService.createUser(testUser);

    // attempt to create second user with same username
    User testUser2 = new User();
    testUser2.setUsername("testUsername");
    testUser2.setPassword("testPassword2");

    // check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2));
  }

  //3. mapping: Get method success
  @Test
  public void getUserById_success() {
    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");
    User createdUser = userService.createUser(testUser);
  
    User foundUser = userService.userProfileById(createdUser.getId());
  
    assertEquals(testUser.getUsername(), foundUser.getUsername());
    }

  //4. mapping: Get method failed
  @Test
  public void getUserById_fail() {
    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");
    userService.createUser(testUser);

    assertThrows(ResponseStatusException.class, () -> userService.userProfileById(1000L));
    }

  //5. mapping: post method success 
  @Test
  public void test_login_success() {
    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");
    userService.createUser(testUser);

    User loginUser = userService.loginUser(testUser);

    assertEquals(testUser.getUsername(), loginUser.getUsername());
    }

  //6. mapping: put method success
  @Test
  public void editUser_success() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date birthday = sdf.parse("2024-03-06");

    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");
    User oldUser = userService.createUser(testUser);
    
    User newUser = new User();
    newUser.setId(oldUser.getId());
    newUser.setUsername("newName");
    newUser.setBirthday(birthday);
    userService.userEditProfile(newUser);
    
    assertEquals(userRepository.findByUsername("newName").getId(), newUser.getId());
    assertEquals(userRepository.findByUsername("newName").getUsername(), newUser.getUsername());
    assertEquals(sdf.format(userRepository.findByUsername("newName").getBirthday()), sdf.format(newUser.getBirthday()));
}


    //7. mapping: put method fail (the user not exist)
    
    @Test
  public void editUser_fail() {
    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");
    userService.createUser(testUser);
    
    User newUser = new User();
    newUser.setId(100000L);
    newUser.setUsername("testUsername1");
    
    assertThrows(ResponseStatusException.class, () -> userService.userEditProfile(newUser));
    }
}
