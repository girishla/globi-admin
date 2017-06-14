package com.globi.infa.workflow;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.globi.infa.generator.service.PTPGeneratorRequestProcessor;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class InfaPTPWorkflowFromMetadataController {
	
	private final PTPGeneratorRequestProcessor requestProcessor;

	InfaPTPWorkflowFromMetadataController(PTPGeneratorRequestProcessor requestProcessor){
		this.requestProcessor=requestProcessor;
		
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/workflows/ptpFromMetadata")
	public @ResponseBody ResponseEntity<?> createPTPExtractWorkflow() {

		List<PTPWorkflow> inputDefinitions=requestProcessor.buildInput();
		List<PTPWorkflow> createdWorkflows=requestProcessor.process(inputDefinitions);
		
		
		return new ResponseEntity<List<PTPWorkflow>>(createdWorkflows, HttpStatus.CREATED);

	}

}
