package ch.uzh.ifi.hase.soprafs24.service;
import ch.uzh.ifi.hase.soprafs24.model.Message;

import org.springframework.beans.factory.annotation.Autowired;
import ch.uzh.ifi.hase.soprafs24.constant.MessageStatus;
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

    public void systemReminder(String reminderInfo,Long roomId) {
        Message reminderMessage = new Message();
        reminderMessage.setSenderName("system");
        reminderMessage.setMessage(reminderInfo);
        reminderMessage.setStatus(MessageStatus.MESSAGE);
        simpMessagingTemplate.convertAndSend("/chatroom/"+roomId+"/public", reminderMessage);
    }

}
