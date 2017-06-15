package com.globi.infa.generator.service;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.globi.infa.AbstractInfaWorkflowEntity;
import com.globi.infa.datasource.core.MetadataTableColumnRepository;
import com.globi.infa.generator.AggregateGitWriterEventListener;
import com.globi.infa.generator.FileWriterEventListener;
import com.globi.infa.generator.GitWriterEventListener;
import com.globi.infa.generator.PTPExtractGenerationStrategy;
import com.globi.infa.generator.PTPPrimaryGenerationStrategy;
import com.globi.infa.metadata.pdl.InfaPuddleDefinitionRepositoryWriter;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PTPGeneratorRequestAsyncProcessor implements GeneratorRequestAsyncProcessor {

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
	public void postProcess() {
		// do nothing

	}
	
    public PTPWorkflow processWorkflow(PTPWorkflow wf){
        log.info(":::::::::::Processing " + wf.getWorkflow().getWorkflowName());

		ptpExtractgenerator.setWfDefinition(wf);
		ptpExtractgenerator.generate();

		ptpPrimarygenerator.setWfDefinition(wf);
		ptpPrimarygenerator.generate();

		wf.getWorkflow().setWorkflowStatus("Processed");
		ptpRepository.save(wf);
        
        return wf;
    }


	@Override
	public AbstractInfaWorkflowEntity saveInput(AbstractInfaWorkflowEntity inputWorkflowDefinition) {
		
		PTPWorkflow wf=(PTPWorkflow)inputWorkflowDefinition;
		
		Optional<PTPWorkflow> existingWorkflow = ptpRepository
				.findByWorkflow_workflowName(wf.getWorkflow().getWorkflowName());
		if (existingWorkflow.isPresent()) {
			existingWorkflow.get().getColumns().clear();
			PTPWorkflow cleanedWf= ptpRepository.save(existingWorkflow.get());
			wf.setId(cleanedWf.getId());
			wf.getWorkflow().setId((cleanedWf.getWorkflow().getId()));
			wf.setVersion(cleanedWf.getVersion());
			wf.getWorkflow().setVersion(cleanedWf.getWorkflow().getVersion());
		}
		wf.getWorkflow().setWorkflowStatus("Queued");
		
		return ptpRepository.save(wf);
	}

	@Override
	@Async
	@Transactional
	public void process(AbstractInfaWorkflowEntity inputWorkflowDefinition) {
		
		
		ptpExtractgenerator.addListener(gitWriter);
		ptpExtractgenerator.addListener(aggregateGitWriter);
		ptpExtractgenerator.addListener(targetDefnWriter);

		ptpPrimarygenerator.addListener(gitWriter);
		ptpPrimarygenerator.addListener(aggregateGitWriter);
		ptpPrimarygenerator.addListener(targetDefnWriter);


		this.processWorkflow((PTPWorkflow)inputWorkflowDefinition);
		
		
	}

}
