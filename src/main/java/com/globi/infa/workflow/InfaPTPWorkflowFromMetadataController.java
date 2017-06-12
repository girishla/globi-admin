package com.globi.infa.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.globi.infa.datasource.core.DataSourceTableColumnDTO;
import com.globi.infa.datasource.core.MetadataTableColumnRepository;
import com.globi.infa.generator.AggregateGitWriterEventListener;
import com.globi.infa.generator.FileWriterEventListener;
import com.globi.infa.generator.GitWriterEventListener;
import com.globi.infa.generator.PTPExtractGenerationStrategy;
import com.globi.infa.generator.PTPPrimaryGenerationStrategy;
import com.globi.infa.metadata.target.InfaTargetDefinitionRepositoryWriter;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class InfaPTPWorkflowFromMetadataController {

	@Autowired
	private PTPWorkflowRepository repository;

	@Autowired
	FileWriterEventListener fileWriter;

	@Autowired
	GitWriterEventListener gitWriter;
	
	@Autowired
	AggregateGitWriterEventListener aggregateGitWriter;

	@Autowired
	private PTPExtractGenerationStrategy ptpExtractgenerator;

	@Autowired
	private PTPPrimaryGenerationStrategy ptpPrimarygenerator;
	
	@Autowired
	private InfaTargetDefinitionRepositoryWriter targetDefnWriter;

	@Autowired
	MetadataTableColumnRepository metadataColumnRepository;

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/workflows/ptpFromMetadata")
	public @ResponseBody ResponseEntity<?> createPTPExtractWorkflow() {

		List<PTPWorkflow> inputWorkflowDefinitions = new ArrayList<>();
		List<PTPWorkflow> createdWorkflows = new ArrayList<>();

		List<DataSourceTableColumnDTO> columns = metadataColumnRepository.getAll();

		MetadataToPTPWorkflowDefnConverter metadatatoWFDefnConverter = new MetadataToPTPWorkflowDefnConverter(
				repository, columns);

		inputWorkflowDefinitions = metadatatoWFDefnConverter.getWorkflowDefinitionObjects();

//		ptpExtractgenerator.addListener(fileWriter);
		ptpExtractgenerator.addListener(gitWriter);
//		ptpExtractgenerator.addListener(aggregateGitWriter);
		ptpExtractgenerator.addListener(targetDefnWriter);		

		
//		ptpPrimarygenerator.addListener(fileWriter);
		ptpPrimarygenerator.addListener(gitWriter);
//		ptpPrimarygenerator.addListener(aggregateGitWriter);
		ptpPrimarygenerator.addListener(targetDefnWriter);				
		
		
		inputWorkflowDefinitions.forEach(wf -> {
			
			ptpExtractgenerator.setWfDefinition(wf);
			ptpExtractgenerator.generate();

			ptpPrimarygenerator.setWfDefinition(wf);
			ptpPrimarygenerator.generate();			
			
			Optional<PTPWorkflow> existingWorkflow = repository.findByWorkflow_workflowName(wf.getWorkflow().getWorkflowName());
			if (existingWorkflow.isPresent()) {
				repository.delete(existingWorkflow.get());
			}
			createdWorkflows.add(repository.save(wf));
		});

		return new ResponseEntity<List<PTPWorkflow>>(createdWorkflows, HttpStatus.CREATED);
	}

}
