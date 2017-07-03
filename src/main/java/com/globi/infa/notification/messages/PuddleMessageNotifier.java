package com.globi.infa.notification.messages;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PuddleMessageNotifier implements MessageNotifier {

	SimpMessagingTemplate template;
	
	
	PuddleMessageNotifier(SimpMessagingTemplate template) {

		this.template=template;
	}

	public void notify(String topic, NotificationMessage msg){
		
		log.info("sending message to all clients");
		template.convertAndSend(topic,(PuddleNotificationContentMessage)msg);
		
	}

}
