package com.globi;



import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Base class to implement transactional integration tests using the root application configuration.
 * 
 * @author Girish Lakshmanan
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@Transactional
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {
	
	
	//Utility method
	protected String asString(JAXBContext pContext, Object pObject) throws JAXBException {

		java.io.StringWriter sw = new StringWriter();

		Marshaller jaxMarshaller = pContext.createMarshaller();
		jaxMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		jaxMarshaller.marshal(pObject, sw);

		return sw.toString();
	}

	
}
