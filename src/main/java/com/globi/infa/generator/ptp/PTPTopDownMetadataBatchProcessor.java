package com.globi.infa.generator.ptp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.jmx.access.InvalidInvocationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.globi.infa.AbstractInfaWorkflowEntity;
import com.globi.infa.datasource.core.PTPDataSourceTableColumnDTO;
import com.globi.infa.generator.AggregateGitWriterEventListener;
import com.globi.infa.generator.AggregatePmcmdFileWriterEventListener;
import com.globi.infa.generator.FileWriterEventListener;
import com.globi.infa.generator.GitWriterEventListener;
import com.globi.infa.generator.service.GeneratorBatchAsyncProcessor;
import com.globi.infa.metadata.pdl.InfaPuddleDefinitionRepositoryWriter;
import com.globi.infa.metadata.ptp.TopDownPTPMetadataTableColumnRepository;
import com.globi.infa.notification.messages.WorkflowMessageNotifier;
import com.globi.infa.workflow.ptp.InfaPTPWorkflowRepository;
import com.globi.infa.workflow.ptp.PTPWorkflow;

import lombok.extern.slf4j.Slf4j;

@Service("PTPBatchProcessor")
@Slf4j
public class PTPTopDownMetadataBatchProcessor implements GeneratorBatchAsyncProcessor {

	@Autowired
	private InfaPTPWorkflowRepository ptpRepository;

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
	TopDownPTPMetadataTableColumnRepository metadataColumnRepository;

	@Autowired
	private WorkflowMessageNotifier notifier;

	@Override
	public List<PTPWorkflow> buildInput() {

		List<PTPWorkflow> inputExtractWorkflowDefinitions = new ArrayList<>();
		List<PTPDataSourceTableColumnDTO> columns = metadataColumnRepository.getAll();
		PTPMetadataToWorkflowDefnConverter metadatatoWFDefnConverter = new PTPMetadataToWorkflowDefnConverter(columns);
		inputExtractWorkflowDefinitions = metadatatoWFDefnConverter.getExtractWorkflowDefinitionObjects();

		return inputExtractWorkflowDefinitions;

	}

	@Override
	public void postProcess() {
		// do nothing

	}

	@Lookup
	//to get a new generator instance for every invocation
	public PTPGenerationStrategy getPtpExtractgenerator() {
		return null; // This implementation will be overridden by dynamically
						// generated subclass
	}

	@Transactional(propagation = Propagation.NESTED)
	public PTPWorkflow processWorkflow(PTPWorkflow wf, PTPGenerationStrategy ptpExtractgenerator) {

		try {

			// Refresh in case someone has modified the wf meanwhile
			wf = ptpRepository.findOne(wf.getId());

			ptpExtractgenerator.generate(wf);
			wf.setWorkflowStatus("Processed");
			this.notifier.message(wf, "Finished processing puddle workflow.");
			
			
		} catch (Exception e) {
			
			log.error(ExceptionUtils.getStackTrace(e));
			
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

		PTPGenerationStrategy ptpExtractgenerator = getPtpExtractgenerator();

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
					this.notifier.message(wf, "Waiting for a new generator thread...");
					wf.setWorkflowStatus("Processing");
					this.notifier.message(wf, "Starting workflow generation.");
					savedWorkflows.add(ptpRepository.save(wf));

				});

		return savedWorkflows;
	}

	@Override
	public AbstractInfaWorkflowEntity buildInputFor(String processType,String itemName) {
		throw new InvalidInvocationException("this is not allowed for PTP");
	}

}
