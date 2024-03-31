package ch.uzh.ifi.hase.soprafs24.service;
import ch.uzh.ifi.hase.soprafs24.model.Message;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import ch.uzh.ifi.hase.soprafs24.constant.MessageType;
import ch.uzh.ifi.hase.soprafs24.constant.PlayerStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SocketService {

    @Autowired
    private final SimpMessagingTemplate simpMessagingTemplate;

    public SocketService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    // public method for system reminder
    public void systemReminder(String reminderInfo,Long roomId) {
        Message reminderMessage = new Message();
        reminderMessage.setSenderName("system");
        reminderMessage.setMessage(reminderInfo);
        reminderMessage.setMessageStatus(MessageType.MESSAGE);
        simpMessagingTemplate.convertAndSend("/room/"+roomId+"/public", reminderMessage);
    }
    
    //broadcast ready message
    public void broadcastReady(Long roomId, boolean isReady) {
        Message readinessMessage = new Message();
        readinessMessage.setTimestamp(LocalDateTime.now());
        readinessMessage.setMessageStatus(MessageType.READY); 
        readinessMessage.setMessage(isReady ? "Ready" : "Not Ready");
        
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", readinessMessage);
    }

}
