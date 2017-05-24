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

import com.globi.infa.DataSourceTable;

@RestController
public class InfaWorkflowController {

	@Autowired
	private InfaWorkflowRepository repository;

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/workflows")
	public @ResponseBody ResponseEntity<?> createWorkflow(@RequestBody DataSourceTable dataSourceTable) {

		InfaWorkflow createdWorkflow = repository.createWorkflow(dataSourceTable);

		return  new ResponseEntity<InfaWorkflow>(createdWorkflow, HttpStatus.CREATED);
	}
	
	
}
