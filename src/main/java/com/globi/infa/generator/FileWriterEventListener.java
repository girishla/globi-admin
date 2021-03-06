package com.globi.infa.generator;

import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.stream.StreamResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;

import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.workflow.GeneratedWorkflow;

@Component
public class FileWriterEventListener implements WorkflowCreatedEventListener {

	@Autowired
	private Jaxb2Marshaller marshaller;
		
	@Value("${temp.dir}")
	private String fileDirectory;

	private void saveXML(Object jaxbObject,String fileName) throws IOException {
		FileOutputStream os = null;
		try {
			
			os = new FileOutputStream(fileDirectory + fileName + ".xml");
			this.marshaller.marshal(jaxbObject, new StreamResult(os));
		} finally {
			if (os != null) {
				os.close();
			}
		}
	}


	@Override
	public void notify(InfaPowermartObject generatedObject,GeneratedWorkflow wf) {

		try {
			this.saveXML(generatedObject.pmObject,generatedObject.pmObjectName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
	}




}
