package com.globi.infa.workflow;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.globi.infa.AbstractInfaWorkflowEntity;
import com.globi.infa.generator.service.GeneratorRequestBatchProcessor;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class InfaPTPWorkflowFromMetadataController {
	
	private final GeneratorRequestBatchProcessor requestProcessor;

	InfaPTPWorkflowFromMetadataController(GeneratorRequestBatchProcessor requestProcessor){
		this.requestProcessor=requestProcessor;
		
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/workflows/ptpFromMetadata")
	public @ResponseBody ResponseEntity<?> createPTPExtractWorkflow() {

		List<? extends AbstractInfaWorkflowEntity> inputDefinitions=requestProcessor.buildInput();
		List<? extends AbstractInfaWorkflowEntity> savedWorkflows=requestProcessor.saveInput(inputDefinitions);
		
		List<PTPWorkflow> responseWorkflows=savedWorkflows.stream()//
				.map(wf->(PTPWorkflow)wf)//
				.collect(Collectors.toList());
		
		requestProcessor.process(savedWorkflows);
		
		return new ResponseEntity<List<PTPWorkflow>>(responseWorkflows, HttpStatus.CREATED);

	}

}
