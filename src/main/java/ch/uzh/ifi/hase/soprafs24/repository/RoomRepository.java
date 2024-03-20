package ch.uzh.ifi.hase.soprafs24.repository;
//import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Room;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;


@Repository("roomRepository")
public interface RoomRepository extends JpaRepository<Room, Long> {
    Room getOne(@RequestParam(required = true) Long id);
}

