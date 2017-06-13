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
import com.globi.infa.metadata.pdl.InfaPuddleDefinitionRepositoryWriter;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class InfaPTPWorkflowFromMetadataController {

	@Autowired
	private PTPWorkflowRepository ptpRepository;



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
	private InfaPuddleDefinitionRepositoryWriter targetDefnWriter;

	@Autowired
	MetadataTableColumnRepository metadataColumnRepository;

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/workflows/ptpFromMetadata")
	public @ResponseBody ResponseEntity<?> createPTPExtractWorkflow() {

		List<PTPWorkflow> inputExtractWorkflowDefinitions = new ArrayList<>();
		List<GeneratedWorkflow> createdWorkflows = new ArrayList<>();

		List<DataSourceTableColumnDTO> columns = metadataColumnRepository.getAll();

		MetadataToPTPWorkflowDefnConverter metadatatoWFDefnConverter = new MetadataToPTPWorkflowDefnConverter(columns);

		inputExtractWorkflowDefinitions = metadatatoWFDefnConverter.getExtractWorkflowDefinitionObjects();

		ptpExtractgenerator.addListener(gitWriter);
		ptpExtractgenerator.addListener(aggregateGitWriter);
		ptpExtractgenerator.addListener(targetDefnWriter);

		ptpPrimarygenerator.addListener(gitWriter);
		ptpPrimarygenerator.addListener(aggregateGitWriter);
		ptpPrimarygenerator.addListener(targetDefnWriter);

		
		aggregateGitWriter.notifyBatchStart();
		
		inputExtractWorkflowDefinitions.stream()//
				.filter(wf -> wf.getWorkflow().getWorkflowType().equals("PTP"))//
				.forEach(wf -> {

					ptpExtractgenerator.setWfDefinition(wf);
					ptpExtractgenerator.generate();

					ptpPrimarygenerator.setWfDefinition(wf);
					ptpPrimarygenerator.generate();
					
					Optional<PTPWorkflow> existingWorkflow = ptpRepository
							.findByWorkflow_workflowName(wf.getWorkflow().getWorkflowName());
					if (existingWorkflow.isPresent()) {
						ptpRepository.delete(existingWorkflow.get());
					}

					createdWorkflows.add(ptpRepository.save(wf));
				});



		aggregateGitWriter.notifyBatchComplete();

		return new ResponseEntity<List<GeneratedWorkflow>>(createdWorkflows, HttpStatus.CREATED);
	}

}
