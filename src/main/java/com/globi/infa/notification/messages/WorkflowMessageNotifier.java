package com.globi.infa.notification.messages;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.globi.infa.workflow.GeneratedWorkflow;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class WorkflowMessageNotifier {

	SimpMessagingTemplate template;
	
	
	
	WorkflowMessageNotifier(SimpMessagingTemplate template) {

		this.template=template;
	}
	
	
	public void message(GeneratedWorkflow wf,String msg){
		
		this.notifyClients(wf, msg);
		wf.getWorkflow().setStatusMessage(msg);
		
	}
	
	
	
	public void notifyClients(GeneratedWorkflow wf,String msg) {
		
		String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH.mm.ss").format(new Date());
		
		this.notify("/topic/workflows", WorkflowNotificationContentMessage.builder()//
				.messageId(UUID.randomUUID())
				.messageStr(timeStamp + "  " + msg)//
				.workflowId((wf.getWorkflow().getId()))//
				.workflowType(wf.getWorkflow().getWorkflowType())
				.workflowStatus(wf.getWorkflow().getWorkflowStatus())
				.build());
	

	}
	
	
	


	public void notify(String topic, NotificationMessage msg){
		
		log.info("sending message to all clients");
		template.convertAndSend(topic,(WorkflowNotificationContentMessage)msg);
		
	}
	
}
