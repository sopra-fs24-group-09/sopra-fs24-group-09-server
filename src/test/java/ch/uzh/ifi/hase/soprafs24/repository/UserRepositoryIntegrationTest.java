//package ch.uzh.ifi.hase.soprafs24.repository;
//
//import ch.uzh.ifi.hase.soprafs24.entity.User;
//import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import java.util.Date;
//
//@DataJpaTest
//public class UserRepositoryIntegrationTest {
//
//    @Autowired
//    private TestEntityManager entityManager;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Test
//    public void findByUsername_success() {
//        // given
//        User user = new User();
//        user.setUsername("username123");
//        user.setPassword("password123");
//        user.setToken("token123");
//        user.setStatus(UserStatus.ONLINE);
//        user.setRegisterDate(new Date());
//        entityManager.persist(user);
//        entityManager.flush();
//
//        // when
//        User found = userRepository.findByUsername(user.getUsername());
//
//        // then
//        assertNotNull(found);
//        assertEquals(user.getUsername(), found.getUsername());
//        assertEquals(user.getPassword(), found.getPassword());
//        assertEquals(user.getToken(), found.getToken());
//        assertEquals(user.getStatus(), found.getStatus());
//        assertEquals(user.getRegisterDate(), found.getRegisterDate());
//    }
//
//    @Test
//    public void getOne_success() {
//        // given
//        User user = new User();
//        user.setUsername("username123");
//        user.setPassword("password123");
//        user.setToken("token123");
//        user.setStatus(UserStatus.ONLINE);
//        user.setRegisterDate(new Date());
//        entityManager.persist(user);
//        entityManager.flush();
//
//        // when
//        User found = userRepository.getOne(user.getId());
//
//        // then
//        assertNotNull(found);
//        assertEquals(user.getUsername(), found.getUsername());
//        assertEquals(user.getPassword(), found.getPassword());
//        assertEquals(user.getToken(), found.getToken());
//        assertEquals(user.getStatus(), found.getStatus());
//        assertEquals(user.getRegisterDate(), found.getRegisterDate());
//    }
//}