package com.globi.infa.notification.messages;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkflowNotificationContentMessage implements NotificationMessage{
	
	private UUID messageId;
	private Long workflowId;
	private String workflowType;
	private String workflowStatus;
	private String messageStr;
	
	
}
