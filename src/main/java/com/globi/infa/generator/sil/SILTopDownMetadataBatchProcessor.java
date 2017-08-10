package com.globi.infa.generator.sil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.globi.infa.AbstractInfaWorkflowEntity;
import com.globi.infa.generator.AggregateGitWriterEventListener;
import com.globi.infa.generator.AggregatePmcmdFileWriterEventListener;
import com.globi.infa.generator.FileWriterEventListener;
import com.globi.infa.generator.GitWriterEventListener;
import com.globi.infa.generator.service.GeneratorBatchAsyncProcessor;
import com.globi.infa.metadata.pdl.InfaPuddleDefinitionRepositoryWriter;
import com.globi.infa.metadata.sil.SilMetadata;
import com.globi.infa.metadata.sil.SilMetadataRepository;
import com.globi.infa.notification.messages.WorkflowMessageNotifier;
import com.globi.infa.workflow.sil.InfaSILWorkflowRepository;
import com.globi.infa.workflow.sil.SILWorkflow;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SILTopDownMetadataBatchProcessor implements GeneratorBatchAsyncProcessor {

	@Autowired
	private InfaSILWorkflowRepository silRepository;

	@Autowired
	FileWriterEventListener fileWriter;

	@Autowired
	GitWriterEventListener gitWriter;

	@Autowired
	AggregateGitWriterEventListener aggregateGitWriter;

	@Autowired
	AggregatePmcmdFileWriterEventListener aggregateCommandWriter;

	@Autowired
	private InfaPuddleDefinitionRepositoryWriter targetDefnWriter;

	@Autowired
	SilMetadataRepository metadataColumnRepository;

	@Autowired
	private WorkflowMessageNotifier notifier;

	@Override
	public List<SILWorkflow> buildInput() {

	
		
		return null;

	}
	
	@Override
	public void postProcess() {
		// do nothing

	}

	@Lookup
	//to get a new generator instance for every invocation
	public SILFactGenerationStrategy getSilFactGenerator() {
		return null; // This implementation will be overridden by dynamically
						// generated subclass
	}

	@Transactional(propagation = Propagation.NESTED)
	public SILWorkflow processWorkflow(SILWorkflow wf, SILFactGenerationStrategy silgenerator) {

		try {

			// Refresh in case someone has modified the wf meanwhile
			wf = silRepository.findOne(wf.getId());

			silgenerator.generate(wf);
			wf.setWorkflowStatus("Processed");
			this.notifier.message(wf, "Finished processing  workflow.");
			
			
		} catch (Exception e) {
			
			log.error(ExceptionUtils.getStackTrace(e));
			
			wf.setWorkflowStatus("Error");
			wf.setStatusMessage(e.getMessage());

			this.notifier.message(wf, "Error processing  workflow");
		}

		silRepository.save(wf);

		return wf;
	}

	@Async
	public void processAsync(List<? extends AbstractInfaWorkflowEntity> inputWorkflowDefinitions) {

		process(inputWorkflowDefinitions);

	}

	@Override
	public List<? extends AbstractInfaWorkflowEntity> process(
			List<? extends AbstractInfaWorkflowEntity> inputWorkflowDefinitions) {

		List<SILWorkflow> processedWorkflows;

		SILFactGenerationStrategy silgenerator = getSilFactGenerator();

		silgenerator.addListener(gitWriter);
		silgenerator.addListener(aggregateGitWriter);
		silgenerator.addListener(aggregateCommandWriter);
		silgenerator.addListener(targetDefnWriter);

		aggregateGitWriter.notifyBatchStart();
		aggregateCommandWriter.notifyBatchStart();

		processedWorkflows = inputWorkflowDefinitions.stream()//
				.map(wf -> (SILWorkflow) wf)//
				.map(wf -> this.processWorkflow(wf, silgenerator))//
				.collect(Collectors.toList());

		aggregateGitWriter.notifyBatchComplete();
		aggregateCommandWriter.notifyBatchComplete();

		return processedWorkflows;

	}

	@Override
	public List<SILWorkflow> saveInput(List<? extends AbstractInfaWorkflowEntity> inputWorkflowDefinitions) {
		List<SILWorkflow> savedWorkflows = new ArrayList<>();

		inputWorkflowDefinitions.stream()//
				.map(wf -> (SILWorkflow) wf)//
				.forEach(wf -> {
					Optional<SILWorkflow> existingWorkflow = silRepository.findByWorkflowName(wf.getWorkflowName());
					if (existingWorkflow.isPresent()) {
						existingWorkflow.get().getColumns().clear();
						silRepository.save(existingWorkflow.get());
						wf.setId(existingWorkflow.get().getId());
						wf.getWorkflow().setId((existingWorkflow.get().getWorkflow().getId()));
						wf.setVersion(existingWorkflow.get().getVersion());
						wf.getWorkflow().setVersion(existingWorkflow.get().getWorkflow().getVersion());
					}
					wf.setMessage("");
					this.notifier.message(wf, "Waiting for a new generator thread...");
					wf.setWorkflowStatus("Processing");
					this.notifier.message(wf, "Starting workflow generation.");
					savedWorkflows.add(silRepository.save(wf));

				});

		return savedWorkflows;
	}

	@Override
	public AbstractInfaWorkflowEntity buildInputFor(String tableName) {
		

		List<SilMetadata> columns = metadataColumnRepository.getAll(tableName);
		MetadataToSILWorkflowDefnConverter metadatatoWFDefnConverter = new MetadataToSILWorkflowDefnConverter(tableName,columns);
		return metadatatoWFDefnConverter.getSilWorkflowDefinition();
		
		
	}

}
