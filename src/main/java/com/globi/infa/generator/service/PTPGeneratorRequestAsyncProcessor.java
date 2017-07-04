package com.globi.infa.generator.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.globi.infa.AbstractInfaWorkflowEntity;
import com.globi.infa.datasource.core.MetadataTableColumnRepository;
import com.globi.infa.generator.AggregateGitWriterEventListener;
import com.globi.infa.generator.AggregatePTPPmcmdFileWriterEventListener;
import com.globi.infa.generator.FileWriterEventListener;
import com.globi.infa.generator.GitWriterEventListener;
import com.globi.infa.generator.PTPExtractGenerationStrategy;
import com.globi.infa.metadata.pdl.InfaPuddleDefinitionRepositoryWriter;
import com.globi.infa.notification.messages.PuddleMessageNotifier;
import com.globi.infa.notification.messages.PuddleNotificationContentMessage;
import com.globi.infa.workflow.GeneratedWorkflow;
import com.globi.infa.workflow.InfaPTPWorkflowRepository;
import com.globi.infa.workflow.InfaWorkflowStatusMessage;
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
	private InfaPuddleDefinitionRepositoryWriter targetDefnWriter;

	@Autowired
	MetadataTableColumnRepository metadataColumnRepository;

	@Autowired
	private PuddleMessageNotifier notifier;

	@Override
	public void postProcess() {
		// do nothing

	}

	private void notifyClients(String endpoint, GeneratedWorkflow wf, String message) {

		notifier.notify("/topic/" + endpoint,
				PuddleNotificationContentMessage.builder()//
						.messageId(UUID.randomUUID())//
						.messageStr(message)//
						.puddleId(wf.getWorkflow().getId())//
						.puddleStatus(wf.getWorkflow().getWorkflowStatus())//
						.build());

	}

	@Transactional(propagation = Propagation.NESTED)
	private PTPWorkflow processWorkflow(PTPWorkflow wf, PTPExtractGenerationStrategy ptpExtractgenerator) {

		ptpExtractgenerator.setWfDefinition(wf);
		
		

		try {

			ptpExtractgenerator.generate();
			wf.setWorkflowStatus("Processed");
			this.notifyClients("puddles", wf, "Finished processing puddle workflow");
			wf.setStatusMessage("Processed Successfully");

		} catch (Exception e) {
			wf.setWorkflowStatus("Error");
			wf.setStatusMessage(e.getMessage().substring(0, 3999));
			this.notifyClients("puddles", wf, "Error processing puddle workflow");
		}

		ptpRepository.save(wf);

		return wf;
	}

	@Lookup
	public PTPExtractGenerationStrategy getPtpExtractgenerator() {
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
		wf.getWorkflow().setWorkflowStatus("Processing");
		wf = ptpRepository.save(wf);
		this.notifyClients("puddles", wf, "Starting workflow generation.");

		return wf;

	}

	@Override
	public AbstractInfaWorkflowEntity process(AbstractInfaWorkflowEntity inputWorkflowDefinition) {

		PTPExtractGenerationStrategy ptpExtractgenerator = getPtpExtractgenerator();

		ptpExtractgenerator.addListener(gitWriter);
		ptpExtractgenerator.addListener(aggregateGitWriter);
		ptpExtractgenerator.addListener(aggregateCommandWriter);
		ptpExtractgenerator.addListener(targetDefnWriter);

		return this.processWorkflow((PTPWorkflow) inputWorkflowDefinition, ptpExtractgenerator);

	}

	@Override
	@Async
	@Transactional
	public void processAsync(AbstractInfaWorkflowEntity inputWorkflowDefinition) {
		this.process(inputWorkflowDefinition);

	}

}
