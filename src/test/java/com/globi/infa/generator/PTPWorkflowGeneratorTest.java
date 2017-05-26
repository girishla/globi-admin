package com.globi.infa.generator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.xml.sax.SAXException;

import com.globi.AbstractIntegrationTest;
import com.globi.infa.generator.ptp.PTPInfaGenerationStrategy;
import com.globi.infa.workflow.InfaWorkflow;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowRepository;

import xjc.POWERMART;

public class PTPWorkflowGeneratorTest extends AbstractIntegrationTest {

	
	@Autowired
	private Jaxb2Marshaller marshaller;
	
	@Autowired
	private PTPInfaGenerationStrategy generator;
	private static final String FILE_NAME = "c:\\temp\\output_file.xml";
	
	@Autowired
	PTPWorkflowRepository wfRepository;
	PTPWorkflow ptpWorkflow;
	static final String sourceTable = "S_ORG_EXT";
	static final String source = "CUK";
	
	
	@Before
	public void setup(){

		ptpWorkflow = PTPWorkflow.builder()//
				.sourceName(source)//
				.sourceTableName(sourceTable)
				.workflow(InfaWorkflow.builder()//
						.workflowScmUri("/GeneratedWorkflows/Repl/" + "PTP_" + sourceTable + ".xml")//
						.workflowName("PTP_" + sourceTable + "_Extract")//
						.workflowType("PTP")//
						.build())
				.build();
				
		
		
	}

	private void saveXML(Object jaxbObject) throws IOException {
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(FILE_NAME);
			this.marshaller.marshal(jaxbObject, new StreamResult(os));
		} finally {
			if (os != null) {
				os.close();
			}
		}
	}

	private String asString(JAXBContext pContext, Object pObject) throws JAXBException {

		java.io.StringWriter sw = new StringWriter();

		Marshaller jaxMarshaller = pContext.createMarshaller();
		jaxMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		jaxMarshaller.marshal(pObject, sw);

		return sw.toString();
	}

	private POWERMART loadControlFileAsObject(String controlFileName) throws FileNotFoundException, IOException {

		FileInputStream is = null;

		try {
			Resource resource = new ClassPathResource("test/" + controlFileName + ".xml");
			is = new FileInputStream(resource.getFile());
			return (POWERMART) marshaller.unmarshal(new StreamSource(is));
		} finally {
			if (is != null) {
				is.close();
			}
		}

	}
	
	@Test
	public void generatesPTPWorkflowForOrgExtTable() throws JAXBException, FileNotFoundException, IOException, SAXException{
		
		
		PTPWorkflow ptpWorkflow = PTPWorkflow.builder()//
				.sourceName(source)//
				.sourceTableName(sourceTable)
				.workflow(InfaWorkflow.builder()//
						.workflowScmUri("/GeneratedWorkflows/Repl/" + "PTP_" + sourceTable + ".xml")//
						.workflowName("PTP_" + sourceTable + "_Extract")//
						.workflowType("PTP")//
						.build())
				.build();
				
		generator.setWfDefinition(ptpWorkflow);
		InfaPowermartObject pmObj=generator.generate();

		String testXML = asString(marshaller.getJaxbContext(), pmObj.pmObject);
		POWERMART controlObj = loadControlFileAsObject("CONTROL_PTP_CUK_S_ORG_EXT_Extract");
		String controlXML = asString(marshaller.getJaxbContext(), controlObj);
		
		this.saveXML(pmObj.pmObject);
		
		assertXMLEqual(controlXML, testXML);
		
		
	}

}
