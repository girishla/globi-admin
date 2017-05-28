package com.globi.infa.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.globi.infa.generator.FileWriterEventListener;
import com.globi.infa.generator.PTPExtractGenerationStrategy;
import com.globi.infa.generator.PTPPrimaryGenerationStrategy;

@RestController
public class InfaPTPWorkflowController {

	@Autowired
	private PTPWorkflowRepository repository;

	@Autowired
	FileWriterEventListener fileWriter;

	@Autowired
	private PTPExtractGenerationStrategy ptpExtractgenerator;
	
	
	@Autowired
	private PTPPrimaryGenerationStrategy ptpPrimarygenerator;
	


	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/workflows/ptpExtract")
	public @ResponseBody ResponseEntity<?> createPTPExtractWorkflow(@RequestBody PTPWorkflow ptpWorkflow) {

		ptpExtractgenerator.setWfDefinition(ptpWorkflow);
		ptpPrimarygenerator.addListener(fileWriter);
		ptpExtractgenerator.generate();



		return new ResponseEntity<PTPWorkflow>(repository.save(ptpWorkflow), HttpStatus.CREATED);
	}
	
	
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/workflows/ptpPrimary")

	public @ResponseBody ResponseEntity<?> createPTPPrimaryWorkflow(@RequestBody PTPWorkflow ptpWorkflow) {

		ptpPrimarygenerator.setWfDefinition(ptpWorkflow);
		ptpPrimarygenerator.addListener(fileWriter);
		
		ptpPrimarygenerator.generate();


		return new ResponseEntity<PTPWorkflow>(repository.save(ptpWorkflow), HttpStatus.CREATED);
	}
	

}
