package ch.uzh.ifi.hase.soprafs24.service;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
// import ch.uzh.ifi.hase.soprafs24.model.Message;
// import ch.uzh.ifi.hase.soprafs24.model.Status;
// import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SocketService {

    //@Autowired
    // private SimpMessagingTemplate simpMessagingTemplate;


    public SocketService() {

    }

    public void initiateGame(Room roomToInitiate,Long roomId) {
    }

    public void broadcastGameStart(Long roomID) {
    }

    public void broadcastGameEnd(Room room,Long roomId) {
        }

    public void broadcastVoteStart(Long roomId) {
    }

    public void systemReminder(String reminderInfo,Long roomId) {
    }

    public void descriptionBroadcast(String userName, Long roomId) {
    }

    public void conductTurn(Room roomToConduct, Long roomId){
    }
}
