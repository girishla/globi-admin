package com.globi.infa.generator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.globi.infa.AbstractInfaWorkflowEntity;
import com.globi.infa.datasource.core.DataSourceTableColumnDTO;
import com.globi.infa.datasource.core.MetadataTableColumnRepository;
import com.globi.infa.generator.AggregateGitWriterEventListener;
import com.globi.infa.generator.AggregatePTPPmcmdFileWriterEventListener;
import com.globi.infa.generator.FileWriterEventListener;
import com.globi.infa.generator.GitWriterEventListener;
import com.globi.infa.generator.PTPExtractGenerationStrategy;
import com.globi.infa.metadata.pdl.InfaPuddleDefinitionRepositoryWriter;
import com.globi.infa.notification.messages.WorkflowMessageNotifier;
import com.globi.infa.workflow.InfaPTPWorkflowRepository;
import com.globi.infa.workflow.MetadataToPTPWorkflowDefnConverter;
import com.globi.infa.workflow.PTPWorkflow;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PTPGeneratorRequestBatchProcessor implements GeneratorRequestBatchAsyncProcessor {

	@Autowired
	private InfaPTPWorkflowRepository ptpRepository;

	@Autowired
	FileWriterEventListener fileWriter;

	@Autowired
	GitWriterEventListener gitWriter;

	@Autowired
	AggregateGitWriterEventListener aggregateGitWriter;

	@Autowired
	AggregatePTPPmcmdFileWriterEventListener aggregateCommandWriter;

	@Autowired
	private InfaPuddleDefinitionRepositoryWriter targetDefnWriter;

	@Autowired
	MetadataTableColumnRepository metadataColumnRepository;

	@Autowired
	private WorkflowMessageNotifier notifier;

	@Override
	public List<PTPWorkflow> buildInput() {

		List<PTPWorkflow> inputExtractWorkflowDefinitions = new ArrayList<>();
		List<DataSourceTableColumnDTO> columns = metadataColumnRepository.getAll();
		MetadataToPTPWorkflowDefnConverter metadatatoWFDefnConverter = new MetadataToPTPWorkflowDefnConverter(columns);
		inputExtractWorkflowDefinitions = metadatatoWFDefnConverter.getExtractWorkflowDefinitionObjects();

		return inputExtractWorkflowDefinitions;

	}

	@Override
	public void postProcess() {
		// do nothing

	}

	@Lookup
	public PTPExtractGenerationStrategy getPtpExtractgenerator() {
		return null; // This implementation will be overridden by dynamically
						// generated subclass
	}

	@Transactional(propagation = Propagation.NESTED)
	public PTPWorkflow processWorkflow(PTPWorkflow wf, PTPExtractGenerationStrategy ptpExtractgenerator) {

		try {

			// Refresh in case someone has modified the wf meanwhile
			wf = ptpRepository.findOne(wf.getId());

			ptpExtractgenerator.setWfDefinition(wf);
			ptpExtractgenerator.generate();
			wf.setWorkflowStatus("Processed");
			this.notifier.message(wf, "Finished processing puddle workflow.");
			
			
		} catch (Exception e) {
			wf.setWorkflowStatus("Error");
			wf.setStatusMessage(e.getMessage());

			this.notifier.message(wf, "Error processing puddle workflow");
		}

		ptpRepository.save(wf);

		return wf;
	}

	@Async
	public void processAsync(List<? extends AbstractInfaWorkflowEntity> inputWorkflowDefinitions) {

		process(inputWorkflowDefinitions);

	}

	@Override
	public List<? extends AbstractInfaWorkflowEntity> process(
			List<? extends AbstractInfaWorkflowEntity> inputWorkflowDefinitions) {

		List<PTPWorkflow> processedWorkflows;

		PTPExtractGenerationStrategy ptpExtractgenerator = getPtpExtractgenerator();

		ptpExtractgenerator.addListener(gitWriter);
		ptpExtractgenerator.addListener(aggregateGitWriter);
		ptpExtractgenerator.addListener(aggregateCommandWriter);
		ptpExtractgenerator.addListener(targetDefnWriter);

		aggregateGitWriter.notifyBatchStart();
		aggregateCommandWriter.notifyBatchStart();

		processedWorkflows = inputWorkflowDefinitions.stream()//
				.map(wf -> (PTPWorkflow) wf)//
				.map(wf -> this.processWorkflow(wf, ptpExtractgenerator))//
				.collect(Collectors.toList());

		aggregateGitWriter.notifyBatchComplete();
		aggregateCommandWriter.notifyBatchComplete();

		return processedWorkflows;

	}

	@Override
	public List<PTPWorkflow> saveInput(List<? extends AbstractInfaWorkflowEntity> inputWorkflowDefinitions) {
		List<PTPWorkflow> savedWorkflows = new ArrayList<>();

		inputWorkflowDefinitions.stream()//
				.map(wf -> (PTPWorkflow) wf)//
				.forEach(wf -> {
					Optional<PTPWorkflow> existingWorkflow = ptpRepository.findByWorkflowName(wf.getWorkflowName());
					if (existingWorkflow.isPresent()) {
						existingWorkflow.get().getColumns().clear();
						ptpRepository.save(existingWorkflow.get());
						wf.setId(existingWorkflow.get().getId());
						wf.getWorkflow().setId((existingWorkflow.get().getWorkflow().getId()));
						wf.setVersion(existingWorkflow.get().getVersion());
						wf.getWorkflow().setVersion(existingWorkflow.get().getWorkflow().getVersion());
					}
					wf.setMessage("");
					wf.getWorkflow().setWorkflowStatus("Queued");
					savedWorkflows.add(ptpRepository.save(wf));

				});

		return savedWorkflows;
	}

}
