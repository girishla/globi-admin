package com.globi.infa.sourcedefinition;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfaSourceDefinitionController {

	@Autowired
	private InfaSourceDefinitionRepository repository;

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/sourceDefinitions")
	public @ResponseBody ResponseEntity<?> createWorkflow(@RequestBody SourceDefinitionInput sourceDefinitionInput) {

		InfaSourceDefinition createdSourceDefinition = repository.createSourceDefiniton(sourceDefinitionInput);

		return  new ResponseEntity<InfaSourceDefinition>(createdSourceDefinition, HttpStatus.CREATED);
	}
	
	
}
