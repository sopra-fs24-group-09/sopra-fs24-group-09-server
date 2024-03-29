//package ch.uzh.ifi.hase.soprafs24.service;
//
//import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
//import ch.uzh.ifi.hase.soprafs24.entity.User;
//import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//import org.springframework.web.server.ResponseStatusException;
//import static org.mockito.BDDMockito.given;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Optional;
//
//public class UserServiceTest {
//
//  @Mock
//  private UserRepository userRepository;
//
//  @InjectMocks
//  private UserService userService;
//
//  private User testUser;
//
//  @BeforeEach
//  public void setup() {
//    MockitoAnnotations.openMocks(this);
//
//    // given
//    testUser = new User();
//    testUser.setId(1L);
//    testUser.setUsername("testUsername");
//    testUser.setPassword("testPassword");
//
//    // when -> any object is being save in the userRepository -> return the dummy
//    // testUser
//    Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
//  }
//
//  @Test
//  public void createUser_validInputs_success() {
//    // when -> any object is being save in the userRepository -> return the dummy
//    // testUser
//    User createdUser = userService.createUser(testUser);
//
//    // then
//    Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
//
//    assertEquals(testUser.getId(), createdUser.getId());
//    assertEquals(testUser.getUsername(), createdUser.getUsername());
//    assertNotNull(createdUser.getToken());
//    assertEquals(UserStatus.ONLINE, createdUser.getStatus());
//  }
//
//  @Test
//  public void createUser_duplicateName_throwsException() {
//      // given -> a first user has already been created
//      User testUser2 = new User();
//      //set the same name but different password
//      testUser2.setUsername("testUsername");
//      testUser2.setPassword("testPassword2");
//
//      userService.createUser(testUser);
//      // when -> setup additional mocks for UserRepository
//      Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);
//
//      // then -> attempt to create second user with same user -> check that an error
//      // is thrown
//      assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2));
//  }
//
//  @Test
//  public void createUser_duplicateInputs_throwsException() {
//    // given -> a first user has already been created
//    User existuser;
//    existuser = new User();
//    existuser.setId(2L);
//    existuser.setUsername("testUsername");
//    userService.createUser(testUser);
//
//    // when -> setup additional mocks for UserRepository
//    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(existuser);
//
//    // then -> attempt to create second user with same user -> check that an error
//    // is thrown
//    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
//  }
//
//
//
// @Test
//  public void getUserById_validInput_success(){
//    // Mock the userRepository to return the testUser object
//    given(userRepository.findById(Mockito.any())).willReturn(java.util.Optional.ofNullable(testUser));
//    User user = new User();
//    user.setId(testUser.getId());
//    // Call the userService method with the testUser's id
//    User foundUser = userService.userProfileById(user.getId());
//
//    // Verify that the userService method returns the correct user object
//    assertEquals(testUser.getId(), foundUser.getId(), "The id does not match");
//    assertEquals(testUser.getUsername(), foundUser.getUsername(), "The username does not match");
//    assertEquals(testUser.getToken(), foundUser.getToken(), "The token does not match");
//}
//
//
//  @Test
//  public void getUserById_invalidInput_fail() {
//      userService.createUser(testUser);
//
//      Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());
//
//      assertThrows( ResponseStatusException.class, () -> userService.userProfileById(10L));
//  }
//
//  @Test
//  public void test_login_success(){
//      User user = new User();
//      user.setUsername(testUser.getUsername());
//      user.setPassword(testUser.getPassword());
//
//      // when -> setup additional mocks for UserRepository
//      Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);
//
//      User loginUser = userService.loginUser(user);
//
//      assertEquals(testUser, loginUser);
//  }
//
//  @Test
//  public void login_fail_username() {
//      userService.createUser(testUser);
//
//      Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);
//
//      assertThrows(ResponseStatusException.class, () -> userService.loginUser(testUser));
//  }
//
//  @Test
//  public void login_fail_password() {
//      userService.createUser(testUser);
//      User user = new User();
//      user.setUsername(testUser.getUsername());
//      user.setPassword("wrong_password");
//
//      Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);
//
//      assertThrows(ResponseStatusException.class, () -> userService.loginUser(user));
//  }
//
//  @Test
//  public void editUsername_Birthday_success() throws Exception {
//      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//      Date birthday = sdf.parse("2024-03-06");
//      User newUser = new User();
//      newUser.setId(1L);
//      newUser.setUsername("testUsername");
//      newUser.setBirthday(birthday);
//      userService.createUser(newUser);
//
//      // when -> any object is being updated in the userRepository -> return the testUpdateUser
//      Mockito.when(userRepository.save(Mockito.any())).thenReturn(newUser);
//      Mockito.when(userRepository.existsById(Mockito.any())).thenReturn(true);
//      Mockito.when(userRepository.getOne(Mockito.any())).thenReturn(testUser);
//
//      // when -> any object is being saved in the userRepository -> return the testUser
//      userService.userEditProfile(newUser);
//
//      // then
//      Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
//
//      assertEquals(testUser.getId(), newUser.getId());
//      assertEquals(testUser.getUsername(), newUser.getUsername());
//      assertEquals(testUser.getBirthday(), newUser.getBirthday());
//  }
//
//}