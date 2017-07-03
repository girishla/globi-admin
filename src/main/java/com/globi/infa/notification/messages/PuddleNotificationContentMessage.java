package com.globi.infa.notification.messages;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PuddleNotificationContentMessage implements NotificationMessage{
	
	private UUID messageId;
	private Long puddleId;
	private String puddleStatus;
	private String messageStr;
	

}
