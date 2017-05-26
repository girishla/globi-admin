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
import com.globi.infa.generator.ptp.PTPInfaGenerationStrategy;

@RestController
public class InfaPTPWorkflowController {

	@Autowired
	private PTPWorkflowRepository repository;

	@Autowired
	private LNICRMTableColumnRepository colRepository;
	
	@Autowired
	private OracleToInfaDataTypeMapper dataTypeMapper;
	
	@Autowired
	private OracleTableColumnMetadataVisitor columnQueryVisitor;
	

	@Autowired
	private Jaxb2Marshaller marshaller;

	@Autowired
	private PTPInfaGenerationStrategy generator;
	private static final String FILE_NAME = "c:\\temp\\output_file.xml";

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

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/workflows/ptp")
	public @ResponseBody ResponseEntity<?> createWorkflow(@RequestBody PTPWorkflow ptpWorkflow) {

		generator.setWfDefinition(ptpWorkflow);
		generator.setColRepository((TableColumnRepository) colRepository);
		generator.setDataTypeMapper(dataTypeMapper);
		generator.setColumnQueryVisitor(columnQueryVisitor);
		
		InfaPowermartObject pmObj = generator.generate();

		try {
			this.saveXML(pmObj.pmObject);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new ResponseEntity<PTPWorkflow>(repository.save(ptpWorkflow), HttpStatus.CREATED);
	}

}
