package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;

public class UserServiceTest {

   @Mock
   private UserRepository userRepository;

   @InjectMocks
   private UserService userService;

   private User testUser;

   @BeforeEach
   public void setup() {
       MockitoAnnotations.openMocks(this);

       // given
       testUser = new User();
       testUser.setId("1");
       testUser.setUsername("testUsername");
       testUser.setPassword("testPassword");

       // when -> any object is being save in the userRepository -> return the dummy
       // testUser
       Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
   }

   @Test
   public void createUser_validInputs_success() {
       // when -> any object is being save in the userRepository -> return the dummy
       // testUser
       User createdUser = userService.createUser(testUser);
       assertEquals(testUser.getId(), createdUser.getId());
       assertEquals(testUser.getUsername(), createdUser.getUsername());
       assertNotNull(createdUser.getToken());
       assertEquals(UserStatus.ONLINE, createdUser.getStatus());
   }

   @Test
   public void createUser_duplicateName_throwsException() {
       // given -> a first user has already been created
       User testUser2 = new User();
       // set the same name but different password
       testUser2.setUsername("testUsername");
       testUser2.setPassword("testPassword2");

       userService.createUser(testUser);
       // when -> setup additional mocks for UserRepository
       Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(Optional.of(testUser)); // Return an
                                                                                                      // existing user

       // then -> attempt to create second user with same user -> check that an error
       // is thrown
       assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2));
   }

   @Test
   public void createUser_duplicateInputs_throwsException() {
       // given -> a first user has already been created
       User existuser;
       existuser = new User();
       existuser.setId("2");
       existuser.setUsername("testUsername");
       userService.createUser(testUser);

       // when -> setup additional mocks for UserRepository
       Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(Optional.of(testUser)); // Return an
                                                                                                      // existing user

       // then -> attempt to create second user with same user -> check that an error
       // is thrown
       assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
   }

   @Test
   public void createUser_Space_include() {
       User testUser = new User();
       testUser.setUsername("test Username");
       testUser.setPassword("testPassword");

       assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
   }

   @Test
    public void createUser_Username_null() {
         User testUser = new User();
         testUser.setUsername(null);
         testUser.setPassword("testPassword");
    
         assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }

    @Test
    public void createUser_Password_null() {
         User testUser = new User();
         testUser.setUsername("testUsername");
         testUser.setPassword(null);
    
         assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }

   @Test
   public void getUserById_validInput_success() {
       // Mock the userRepository to return the testUser object
       given(userRepository.findById(Mockito.any())).willReturn(java.util.Optional.ofNullable(testUser));
       User user = new User();
       user.setId(testUser.getId());
       // Call the userService method with the testUser's id
       User foundUser = userService.userProfileById(user.getId());

       // Verify that the userService method returns the correct user object
       assertEquals(testUser.getId(), foundUser.getId(), "The id does not match");
       assertEquals(testUser.getUsername(), foundUser.getUsername(), "The username does not match");
       assertEquals(testUser.getToken(), foundUser.getToken(), "The token does not match");
   }

   @Test
   public void getUserById_invalidInput_fail() {
       userService.createUser(testUser);

       Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());

       assertThrows(ResponseStatusException.class, () -> userService.userProfileById("10"));
   }

   @Test
   public void test_login_success() {
       User user = new User();
       user.setUsername(testUser.getUsername());
       user.setPassword(testUser.getPassword());

       // when -> setup additional mocks for UserRepository
       Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(Optional.of(testUser)); // Return an
                                                                                                      // existing user

       User loginUser = userService.loginUser(user);

       assertEquals(testUser, loginUser);
   }

   @Test
   public void loginUser_UserNotFound_ThrowsException() {
       User testUser = new User();
       testUser.setUsername("nonexistentUser");

       Mockito.when(userRepository.findByUsername("nonexistentUser")).thenReturn(Optional.empty());

       assertThrows(ResponseStatusException.class,
               () -> userService.loginUser(testUser),
               "Expected loginUser to throw an exception for non-existent user");
   }

   @Test
   public void login_fail_password() {
       userService.createUser(testUser);
       User user = new User();
       user.setUsername(testUser.getUsername());
       user.setPassword("wrong_password");

       Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(Optional.of(testUser)); // Return an
                                                                                                      // existing user

       assertThrows(ResponseStatusException.class, () -> userService.loginUser(user));
   }

   @Test
   public void logoutUser_UserNotFound_ThrowsException() {
       User testUser = new User();
       testUser.setId("nonexistentUserId");

       Mockito.when(userRepository.findById("nonexistentUserId")).thenReturn(Optional.empty());

       assertThrows(ResponseStatusException.class,
               () -> userService.logoutUser(testUser),
               "Expected logoutUser to throw an exception for non-existent user");
   }

   @Test
   public void logoutUser_UserAlreadyLoggedOut_ThrowsException() {
       User testUser = new User();
       testUser.setId("userId");
       testUser.setStatus(UserStatus.OFFLINE);

       Mockito.when(userRepository.findById("userId")).thenReturn(Optional.of(testUser));

       assertThrows(ResponseStatusException.class,
               () -> userService.logoutUser(testUser),
               "Expected logoutUser to throw an exception for already logged out user");
   }

   @Test
   public void logoutUser_SuccessfulLogout_ReturnsNull() {
       User testUser = new User();
       testUser.setId("userId");
       testUser.setStatus(UserStatus.ONLINE);

       Mockito.when(userRepository.findById("userId")).thenReturn(Optional.of(testUser));

       assertNull(userService.logoutUser(testUser));
       assertEquals(UserStatus.OFFLINE, testUser.getStatus());
       Mockito.verify(userRepository).save(testUser);
   }

   @Test
   public void logoutUser_ErrorDuringSave_ThrowsException() {
       User testUser = new User();
       testUser.setId("userId");
       testUser.setStatus(UserStatus.ONLINE);

       Mockito.when(userRepository.findById("userId")).thenReturn(Optional.of(testUser));
       doThrow(new RuntimeException("Database error")).when(userRepository).save(any(User.class));

       assertThrows(ResponseStatusException.class,
               () -> userService.logoutUser(testUser),
               "Expected logoutUser to throw an exception during save operation");
   }

   @Test
   public void userEditProfile_UserNotFound_ThrowsException() {
       Mockito.when(userRepository.existsById("userId")).thenReturn(false);
       assertThrows(ResponseStatusException.class,
               () -> userService.userEditProfile("userId", new User()),
               "Should throw for non-existing user ID");
   }

   @Test
   public void userEditProfile_UpdateUsername_Success() {
       String userId = "userId";
       User existingUser = new User();
       existingUser.setUsername("oldUsername");

       User newUserDetails = new User();
       newUserDetails.setUsername("newUsername");

       Mockito.when(userRepository.existsById(userId)).thenReturn(true);
       Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

       userService.userEditProfile(userId, newUserDetails);

       Mockito.verify(userRepository).save(existingUser);
       assertEquals("newUsername", existingUser.getUsername(), "Username should be updated");
   }

   @Test
   public void userEditProfile_UpdateAvatar_Success() {
       String userId = "userId";
       User existingUser = new User();
       existingUser.setAvatar("oldAvatar.jpg");

       User newUserDetails = new User();
       newUserDetails.setAvatar("newAvatar.jpg");

       Mockito.when(userRepository.existsById(userId)).thenReturn(true);
       Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

       userService.userEditProfile(userId, newUserDetails);

       Mockito.verify(userRepository).save(existingUser);
       assertEquals("newAvatar.jpg", existingUser.getAvatar(), "Avatar should be updated");
   }

    @Test
    void findUserById_UserExists_ReturnsUser() {
        String userId = "userId1";
        User expectedUser = new User();
        expectedUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        User actualUser = userService.findUserById(userId);

        assertNotNull(actualUser);
        assertEquals(expectedUser, actualUser);
        verify(userRepository, times(2)).findById(userId);
    }

    @Test
    void findUserById_UserDoesNotExist_ThrowsException() {
        String userId = "userId1";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.findUserById(userId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found", exception.getReason());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findByToken_TokenExists_ReturnsTrue() {
        String token = "validToken";
        User expectedUser = new User();
        expectedUser.setToken(token);

        when(userRepository.findByToken(token)).thenReturn(expectedUser);

        boolean exists = userService.findByToken(token);

        assertTrue(exists);
        verify(userRepository, times(1)).findByToken(token);
    }

    @Test
    void findByToken_TokenDoesNotExist_ReturnsFalse() {
        String token = "invalidToken";

        when(userRepository.findByToken(token)).thenReturn(null);

        boolean exists = userService.findByToken(token);

        assertFalse(exists);
        verify(userRepository, times(1)).findByToken(token);
    }


}