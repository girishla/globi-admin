package com.globi.infa;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.Marshaller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class InfaConfig {

	@Bean
	public Jaxb2Marshaller jaxb2Marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setPackagesToScan("xjc");
		Map<String, Object> marshallerProps = new HashMap<String, Object>();
		marshallerProps.put("jaxb.formatted.output", true);
		
		marshallerProps.put(Marshaller.JAXB_FRAGMENT, true);
		marshallerProps.put("com.sun.xml.bind.xmlHeaders","<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE POWERMART SYSTEM \"powrmart.dtd\">");
		marshaller.setMarshallerProperties(marshallerProps);
		marshaller.setSupportDtd(true);
		return marshaller;
	}

}
