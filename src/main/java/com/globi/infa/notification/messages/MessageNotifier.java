package com.globi.infa.notification.messages;

public interface MessageNotifier {

	public void notify(String topic, NotificationMessage msg);

}
