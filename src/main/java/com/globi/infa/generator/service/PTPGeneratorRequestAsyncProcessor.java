package com.globi.infa.generator.service;

import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.globi.infa.AbstractInfaWorkflowEntity;
import com.globi.infa.datasource.core.TopDownMetadataTableColumnRepository;
import com.globi.infa.generator.AggregateGitWriterEventListener;
import com.globi.infa.generator.AggregatePTPPmcmdFileWriterEventListener;
import com.globi.infa.generator.FileWriterEventListener;
import com.globi.infa.generator.GitWriterEventListener;
import com.globi.infa.generator.ptp.PTPGenerationStrategy;
import com.globi.infa.metadata.pdl.InfaPuddleDefinitionRepositoryWriter;
import com.globi.infa.notification.messages.WorkflowMessageNotifier;
import com.globi.infa.workflow.InfaPTPWorkflowRepository;
import com.globi.infa.workflow.PTPWorkflow;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PTPGeneratorRequestAsyncProcessor implements GeneratorRequestAsyncProcessor {

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
	TopDownMetadataTableColumnRepository metadataColumnRepository;

	@Autowired
	private WorkflowMessageNotifier notifier;

	@Override
	public void postProcess() {
		// do nothing
	}

	@Transactional(propagation = Propagation.NESTED)
	private PTPWorkflow processWorkflow(PTPWorkflow wf, PTPGenerationStrategy ptpExtractgenerator) {


		this.notifier.message(wf, "Starting workflow generation...");
		ptpExtractgenerator.generate(wf);
		wf.setWorkflowStatus("Processed");
		this.notifier.message(wf, "Finished processing puddle workflow");
		ptpRepository.save(wf);

		return wf;
	}

	@Lookup
	public PTPGenerationStrategy getPtpExtractgenerator() {
		return null; // This implementation will be overridden by dynamically
						// generated subclass
	}

	@Override
	public AbstractInfaWorkflowEntity saveInput(AbstractInfaWorkflowEntity inputWorkflowDefinition) {

		PTPWorkflow wf = (PTPWorkflow) inputWorkflowDefinition;

		Optional<PTPWorkflow> existingWorkflow = ptpRepository.findByWorkflowName(wf.getWorkflow().getWorkflowName());
		if (existingWorkflow.isPresent()) {
			existingWorkflow.get().getColumns().clear();
			PTPWorkflow cleanedWf = ptpRepository.save(existingWorkflow.get());
			wf.setId(cleanedWf.getId());
			wf.getWorkflow().setId((cleanedWf.getWorkflow().getId()));
			wf.setVersion(cleanedWf.getVersion());
			wf.getWorkflow().setVersion(cleanedWf.getWorkflow().getVersion());
		}
		wf.setMessage("");
		wf.setWorkflowStatus("Processing");
		this.notifier.message(wf, "Waiting for a new generator thread...");
	
		wf = ptpRepository.save(wf);

		return wf;

	}

	@Override
	public AbstractInfaWorkflowEntity process(AbstractInfaWorkflowEntity inputWorkflowDefinition) {

		PTPWorkflow wf = (PTPWorkflow) inputWorkflowDefinition;
		PTPGenerationStrategy ptpExtractgenerator = getPtpExtractgenerator();

		ptpExtractgenerator.addListener(gitWriter);
		ptpExtractgenerator.addListener(aggregateGitWriter);
		ptpExtractgenerator.addListener(aggregateCommandWriter);

		
		try {

			wf = this.processWorkflow(wf, ptpExtractgenerator);

		} catch (Exception e) {

			e.printStackTrace();
			log.error(ExceptionUtils.getStackTrace(e));
			wf.setWorkflowStatus("Error");
			this.notifier.message(wf, "Error processing puddle workflow: " + ExceptionUtils.getMessage(e));
		}
		
		ptpRepository.save(wf);
		
		return wf;

	}

	@Override
	@Async
	public void processAsync(AbstractInfaWorkflowEntity inputWorkflowDefinition) {

		
		//refresh in case someone updated before the async thread picks it up
		PTPWorkflow existingWF = ptpRepository.findOne(inputWorkflowDefinition.getId());
		if(existingWF!=null){
			inputWorkflowDefinition=existingWF;
		}
		
		this.process(inputWorkflowDefinition);

	}

}
