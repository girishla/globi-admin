package com.globi.infa.generator.service;

import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.globi.infa.AbstractInfaWorkflowEntity;
import com.globi.infa.generator.AggregateGitWriterEventListener;
import com.globi.infa.generator.AggregatePTPPmcmdFileWriterEventListener;
import com.globi.infa.generator.FileWriterEventListener;
import com.globi.infa.generator.GitWriterEventListener;
import com.globi.infa.generator.InfaGenerationStrategy;
import com.globi.infa.metadata.topdown.TopDownMetadataTableColumnRepository;
import com.globi.infa.notification.messages.WorkflowMessageNotifier;
import com.globi.infa.workflow.InfaWorkflow;
import com.globi.infa.workflow.InfaWorkflowRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DefaultGeneratorRequestAsyncProcessor implements GeneratorRequestAsyncProcessor {

	@Autowired
	private InfaWorkflowRepository wfRepository;

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
	private InfaWorkflow processWorkflow(InfaWorkflow wf, InfaGenerationStrategy generator) {


		this.notifier.message(wf, "Starting workflow generation...");
		generator.generate(wf);
		wf.setWorkflowStatus("Processed");
		this.notifier.message(wf, "Finished processing workflow");
		wfRepository.save(wf);

		return wf;
	}

	@Override
	public AbstractInfaWorkflowEntity saveInput(AbstractInfaWorkflowEntity inputWorkflowDefinition) {

		InfaWorkflow wf = (InfaWorkflow) inputWorkflowDefinition;

		Optional<InfaWorkflow> existingWorkflow = wfRepository.findByWorkflowName(wf.getWorkflowName());
		
		if (existingWorkflow.isPresent()) {
			wfRepository.delete(existingWorkflow.get());

		}
		wf.setMessage("");
		wf.setWorkflowStatus("Processing");
		this.notifier.message(wf, "Waiting for a new generator thread...");
	
		wf = wfRepository.save(wf);

		return wf;

	}

	@Override
	public AbstractInfaWorkflowEntity process(AbstractInfaWorkflowEntity inputWorkflowDefinition,InfaGenerationStrategy generator) {

		InfaWorkflow wf = (InfaWorkflow) inputWorkflowDefinition;


		generator.addListener(gitWriter);
		generator.addListener(aggregateGitWriter);
		generator.addListener(aggregateCommandWriter);

		
		
		try {

			wf = this.processWorkflow(wf, generator);

		} catch (Exception e) {

			e.printStackTrace();
			log.error(ExceptionUtils.getStackTrace(e));
			wf.setWorkflowStatus("Error");
			this.notifier.message(wf, "Error processing request: " + ExceptionUtils.getMessage(e));
		}
		
		wfRepository.save(wf);
		
		return wf;

	}

	@Override
	@Async
	public void processAsync(AbstractInfaWorkflowEntity inputWorkflowDefinition,InfaGenerationStrategy generator) {

		
		//refresh in case someone updated before the async thread picks it up
		InfaWorkflow existingWF = wfRepository.findOne(inputWorkflowDefinition.getId());
		if(existingWF!=null){
			inputWorkflowDefinition=existingWF;
		}
		
		this.process(inputWorkflowDefinition,generator);

	}



}
