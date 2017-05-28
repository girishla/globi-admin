package com.globi.infa.workflow;

import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.stream.StreamResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.globi.infa.datasource.core.OracleTableColumnMetadataVisitor;
import com.globi.infa.datasource.core.OracleToInfaDataTypeMapper;
import com.globi.infa.datasource.core.TableColumnRepository;
import com.globi.infa.datasource.lnicrm.LNICRMTableColumnRepository;
import com.globi.infa.generator.InfaPowermartObject;

@RestController
public class InfaPTPWorkflowController {

	@Autowired
	private PTPWorkflowRepository repository;


	@Autowired
	private Jaxb2Marshaller marshaller;

	@Autowired
	private PTPExtractGenerationStrategy ptpExtractgenerator;
	
	
	@Autowired
	private PTPPrimaryGenerationStrategy ptpPrimarygenerator;
	
	private static final String FILE_DIR = "c:\\temp\\";

	private void saveXML(Object jaxbObject,String fileName) throws IOException {
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(FILE_DIR + fileName + ".xml");
			this.marshaller.marshal(jaxbObject, new StreamResult(os));
		} finally {
			if (os != null) {
				os.close();
			}
		}
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/workflows/ptpExtract")
	public @ResponseBody ResponseEntity<?> createPTPExtractWorkflow(@RequestBody PTPWorkflow ptpWorkflow) {

		ptpExtractgenerator.setWfDefinition(ptpWorkflow);
		
		InfaPowermartObject pmObj = ptpExtractgenerator.generate();

		try {
			this.saveXML(pmObj.pmObject,"PTP_Extract_WebTest");
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		return new ResponseEntity<PTPWorkflow>(repository.save(ptpWorkflow), HttpStatus.CREATED);
	}
	
	
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/workflows/ptpPrimary")

	public @ResponseBody ResponseEntity<?> createPTPPrimaryWorkflow(@RequestBody PTPWorkflow ptpWorkflow) {

		ptpPrimarygenerator.setWfDefinition(ptpWorkflow);
		
		InfaPowermartObject pmObj = ptpPrimarygenerator.generate();

		try {
			this.saveXML(pmObj.pmObject,"PTP_Primary_WebTest");
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		return new ResponseEntity<PTPWorkflow>(repository.save(ptpWorkflow), HttpStatus.CREATED);
	}
	

}
