//package ch.uzh.ifi.hase.soprafs24.service;
//
//import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
//import ch.uzh.ifi.hase.soprafs24.entity.User;
//import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.web.server.ResponseStatusException;
//import static org.mockito.Mockito.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.never;
//
//import java.util.Optional;
//
//public class UserServiceIntegrationTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @InjectMocks
//    private UserService userService;
//
//    @BeforeEach
//    public void setup() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    public void createUser_duplicateUsername_throwsException() {
//        User testUser = new User();
//        testUser.setUsername("testUsername");
//        testUser.setPassword("originalPassword");
//
//        when(userRepository.findByUsername("testUsername")).thenReturn(Optional.of(new User())); // Simulate existing
//                                                                                                 // user
//
//        User newUser = new User();
//        newUser.setUsername("testUsername");
//        newUser.setPassword("testPassword2");
//
//        assertThrows(ResponseStatusException.class, () -> userService.createUser(newUser));
//        verify(userRepository, never()).save(newUser); // Ensure save is never called
//    }
//
//    @Test
//    public void getUserById_success() {
//        User testUser = new User();
//        testUser.setId("1");
//        testUser.setUsername("testUsername");
//        testUser.setPassword("testPassword");
//
//        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
//
//        User foundUser = userService.userProfileById("1");
//
//        assertNotNull(foundUser);
//        assertEquals("testUsername", foundUser.getUsername());
//    }
//
//    @Test
//    public void getUserById_fail() {
//        when(userRepository.findById("nonExistingId")).thenReturn(Optional.empty());
//
//        assertThrows(ResponseStatusException.class, () -> userService.userProfileById("nonExistingId"));
//    }
//
//    @Test
//    public void test_login_success() {
//        User testUser = new User();
//        testUser.setUsername("testUsername");
//        testUser.setPassword("testPassword");
//
//        when(userRepository.findByUsername("testUsername")).thenReturn(Optional.of(testUser));
//
//        User loginUser = userService.loginUser(testUser);
//
//        assertEquals("testUsername", loginUser.getUsername());
//        assertEquals(UserStatus.ONLINE, loginUser.getStatus());
//    }
//
//    @Test
//    public void test_login_fail() {
//        User testUser = new User();
//        testUser.setUsername("testUsername");
//        testUser.setPassword("testPassword");
//
//        when(userRepository.findByUsername("testUsername")).thenReturn(Optional.of(testUser));
//
//        User wrongPasswordUser = new User();
//        wrongPasswordUser.setUsername("testUsername");
//        wrongPasswordUser.setPassword("wrongPassword");
//
//        assertThrows(ResponseStatusException.class, () -> userService.loginUser(wrongPasswordUser));
//    }
//}