package com.globi.infa.notification.messages;


import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomSubProtocolWebSocketHandler extends SubProtocolWebSocketHandler {


//    @Autowired
//    private SessionHandler sessionHandler;

    public CustomSubProtocolWebSocketHandler(MessageChannel clientInboundChannel, SubscribableChannel clientOutboundChannel) {
        super(clientInboundChannel, clientOutboundChannel);
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("New websocket connection was established");
//        sessionHandler.register(session);
        super.afterConnectionEstablished(session);
    }
}