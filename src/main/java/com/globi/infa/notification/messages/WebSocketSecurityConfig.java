//package com.globi.infa.notification.messages;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
//import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
//
//@Configuration
//public class WebSocketSecurityConfig
//      extends AbstractSecurityWebSocketMessageBrokerConfigurer { 
//
//	 @Override
//	    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
//	        messages
//	                .nullDestMatcher().authenticated() 
//	                .simpSubscribeDestMatchers("/workflows").permitAll() 
//	                .simpDestMatchers("/workflows/**").permitAll() 
//	                .simpMessageDestMatchers("/**").permitAll()
//	                .anyMessage().permitAll();
//
//	    }
//}