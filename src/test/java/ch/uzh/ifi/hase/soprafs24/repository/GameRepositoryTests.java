// package ch.uzh.ifi.hase.soprafs24.repository;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;

// import ch.uzh.ifi.hase.soprafs24.entity.Game;
// import ch.uzh.ifi.hase.soprafs24.entity.Room;
// import java.util.Optional;

// @SpringBootTest
// public class GameRepositoryTests {

//     @Autowired
//     private GameRepository gameRepository;

//     @Test
//     public void testFindByRoomId() {
//         Room room = new Room();
//         Game game = new Game(room);
//         game.setRoomId("testRoomId");
//         gameRepository.save(game);

//         Optional<Game> foundGame = gameRepository.findByRoomId("testRoomId");
//         assert(foundGame.isPresent());
//     }
// }
