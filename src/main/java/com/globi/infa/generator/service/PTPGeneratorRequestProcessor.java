package com.globi.infa.generator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.globi.infa.AbstractInfaWorkflowEntity;
import com.globi.infa.datasource.core.DataSourceTableColumnDTO;
import com.globi.infa.datasource.core.MetadataTableColumnRepository;
import com.globi.infa.generator.AggregateGitWriterEventListener;
import com.globi.infa.generator.FileWriterEventListener;
import com.globi.infa.generator.GitWriterEventListener;
import com.globi.infa.generator.PTPExtractGenerationStrategy;
import com.globi.infa.generator.PTPPrimaryGenerationStrategy;
import com.globi.infa.metadata.pdl.InfaPuddleDefinitionRepositoryWriter;
import com.globi.infa.workflow.MetadataToPTPWorkflowDefnConverter;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowRepository;


@Service
public class PTPGeneratorRequestProcessor implements GeneratorRequestProcessor {

	
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

	
	
	@Override
	public List<PTPWorkflow> buildInput() {
		
		List<PTPWorkflow> inputExtractWorkflowDefinitions = new ArrayList<>();
		List<DataSourceTableColumnDTO> columns = metadataColumnRepository.getAll();
		MetadataToPTPWorkflowDefnConverter metadatatoWFDefnConverter = new MetadataToPTPWorkflowDefnConverter(columns);
		inputExtractWorkflowDefinitions = metadatatoWFDefnConverter.getExtractWorkflowDefinitionObjects();
		
		return  inputExtractWorkflowDefinitions;
		
	}
	

	
	@Override
	public void postProcess() {
		// do nothing

	}





	@Override
	public List<PTPWorkflow> process(List<? extends AbstractInfaWorkflowEntity> inputExtractWorkflowDefinitions) {
		
		List<PTPWorkflow> createdWorkflows = new ArrayList<>();
		
			
			ptpExtractgenerator.addListener(gitWriter);
			ptpExtractgenerator.addListener(aggregateGitWriter);
			ptpExtractgenerator.addListener(targetDefnWriter);

			ptpPrimarygenerator.addListener(gitWriter);
			ptpPrimarygenerator.addListener(aggregateGitWriter);
			ptpPrimarygenerator.addListener(targetDefnWriter);

			
			aggregateGitWriter.notifyBatchStart();
			
			inputExtractWorkflowDefinitions.stream()//
					.map(wf->(PTPWorkflow)wf)
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
			
			
			return createdWorkflows;
	}







}
