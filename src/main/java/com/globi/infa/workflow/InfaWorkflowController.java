package com.globi.infa.workflow;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.globi.infa.DataSourceTableDTO;

@RestController
public class InfaWorkflowController {

	@Autowired
	private PTPWorkflowRepository repository;

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/workflows/ptp")
	public @ResponseBody ResponseEntity<?> createWorkflow(@RequestBody PTPWorkflow ptpWorkflow) {

		PTPWorkflow createdWorkflow = repository.createWorkflow(ptpWorkflow);

		return  new ResponseEntity<PTPWorkflow>(createdWorkflow, HttpStatus.CREATED);
	}
	
	
}
