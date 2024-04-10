package ch.uzh.ifi.hase.soprafs24.service;
import ch.uzh.ifi.hase.soprafs24.model.Message;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import ch.uzh.ifi.hase.soprafs24.constant.MessageOrderType;
import ch.uzh.ifi.hase.soprafs24.entity.User;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SocketService {

    @Autowired
    private final SimpMessagingTemplate simpMessagingTemplate;
    private UserRepository userRepository;

    public SocketService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    // public method for system reminder
    public void systemReminder(String reminderInfo,Long roomId) {
        Message reminderMessage = new Message();
        reminderMessage.setSenderName("system");
        reminderMessage.setMessage(reminderInfo);
        reminderMessage.setMessageType(MessageOrderType.MESSAGE);
        simpMessagingTemplate.convertAndSend("/room/"+roomId+"/public", reminderMessage);
    }
    
    //broadcast ready message
    public void broadcastReady(Long roomId, boolean isReady) {
        Message readinessMessage = new Message();
        readinessMessage.setSenderName("system");
        readinessMessage.setSenderName("system");
        readinessMessage.setTimestamp(LocalDateTime.now());
        readinessMessage.setMessageType(MessageOrderType.READY); 
        readinessMessage.setMessage(isReady ? "Ready" : "Not Ready");
        
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", readinessMessage);
    }

    //broadcast unready message
    public void broadcastUnReady(Long roomId, boolean isReady) {
        Message readinessMessage = new Message();
        readinessMessage.setTimestamp(LocalDateTime.now());
        readinessMessage.setMessageType(MessageOrderType.UNREADY); 
        readinessMessage.setMessage(isReady ? "Ready" : "Not Ready");
        
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", readinessMessage);
    }

    //broadcast enterroom message
    public void broadcastEnterroom(String roomId,String userid) {
        Message readinessMessage = new Message();
        readinessMessage.setSenderName("system");
        readinessMessage.setTimestamp(LocalDateTime.now());
        readinessMessage.setMessageType(MessageOrderType.UNREADY); 
        Optional<User> userOptional = userRepository.findById(userid);
        String message;
        if ( userOptional.isPresent()) {
            User user = userOptional.get();
            message = user.getUsername() + " has entered the room.";
        } else {
            message = "Unknown user entered the room.";
        }
        readinessMessage.setMessage(message);
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", readinessMessage);
    }


    //broadcast enterroom message
    public void broadcastExitroom(String roomId,String userid) {
        Message readinessMessage = new Message();
        readinessMessage.setSenderName("system");
        readinessMessage.setTimestamp(LocalDateTime.now());
        readinessMessage.setMessageType(MessageOrderType.UNREADY); 
        Optional<User> userOptional = userRepository.findById(userid);
        String message;
        if ( userOptional.isPresent()) {
            User user = userOptional.get();
            message = user.getUsername() + " has left the room.";
        } else {
            message = "Unknown user left the room.";
        }
        readinessMessage.setMessage(message);
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", readinessMessage);
    }

    //broadcast game start message
    public void broadcastGamestart(String roomId) {
        Message readinessMessage = new Message();
        readinessMessage.setSenderName("system");
        readinessMessage.setTimestamp(LocalDateTime.now());
        readinessMessage.setMessageType(MessageOrderType.GAMESTART); 
        readinessMessage.setMessage("Game Start!");
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", readinessMessage);
    }

}
