package com.globi.infa.workflow;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.globi.infa.generator.service.GeneratorRequestAsyncProcessor;

@RestController
public class InfaPTPWorkflowController {

	private final GeneratorRequestAsyncProcessor requestProcessor;

	InfaPTPWorkflowController(GeneratorRequestAsyncProcessor requestProcessor){
		this.requestProcessor=requestProcessor;
		
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/workflows/ptp")
	public @ResponseBody ResponseEntity<?> createPTPExtractWorkflow(@RequestBody @Valid PTPWorkflow ptpWorkflow) {

		PTPWorkflow savedWorkflow=(PTPWorkflow)requestProcessor.saveInput(ptpWorkflow);
		requestProcessor.process(savedWorkflow);

		return new ResponseEntity<PTPWorkflow>(savedWorkflow, HttpStatus.CREATED);
	}

	

}
