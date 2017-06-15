package com.globi.infa.generator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.scheduling.annotation.Async;
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
import com.globi.infa.workflow.PTPWorkflowSourceColumn;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PTPGeneratorRequestBatchProcessor implements GeneratorRequestBatchProcessor {

	@Autowired
	private PTPWorkflowRepository ptpRepository;

	@Autowired
	FileWriterEventListener fileWriter;

	@Autowired
	GitWriterEventListener gitWriter;

	@Autowired
	AggregateGitWriterEventListener aggregateGitWriter;

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

	@Lookup
	public PTPPrimaryGenerationStrategy getPtpPrimarygenerator() {
		return null; // This implementation will be overridden by dynamically
						// generated subclass
	}

	public PTPWorkflow processWorkflow(PTPWorkflow wf, PTPExtractGenerationStrategy ptpExtractgenerator,
			PTPPrimaryGenerationStrategy ptpPrimarygenerator) {
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
	@Async
	@Transactional
	public void process(List<? extends AbstractInfaWorkflowEntity> inputWorkflowDefinitions) {

		PTPExtractGenerationStrategy ptpExtractgenerator = getPtpExtractgenerator();
		PTPPrimaryGenerationStrategy ptpPrimarygenerator = getPtpPrimarygenerator();

		ptpExtractgenerator.addListener(gitWriter);
		ptpExtractgenerator.addListener(aggregateGitWriter);
		ptpExtractgenerator.addListener(targetDefnWriter);

		ptpPrimarygenerator.addListener(gitWriter);
		ptpPrimarygenerator.addListener(aggregateGitWriter);
		ptpPrimarygenerator.addListener(targetDefnWriter);

		aggregateGitWriter.notifyBatchStart();

		inputWorkflowDefinitions.stream()//
				.map(wf -> (PTPWorkflow) wf)//
				.filter(wf -> wf.getWorkflow().getWorkflowType().equals("PTP"))//
				.forEach(wf -> this.processWorkflow(wf, ptpExtractgenerator, ptpPrimarygenerator));

		aggregateGitWriter.notifyBatchComplete();

	}

	@Override
	public List<PTPWorkflow> saveInput(List<? extends AbstractInfaWorkflowEntity> inputWorkflowDefinitions) {
		List<PTPWorkflow> savedWorkflows = new ArrayList<>();

		inputWorkflowDefinitions.stream()//
				.map(wf -> (PTPWorkflow) wf).filter(wf -> wf.getWorkflow().getWorkflowType().equals("PTP"))//
				.forEach(wf -> {
					Optional<PTPWorkflow> existingWorkflow = ptpRepository
							.findByWorkflow_workflowName(wf.getWorkflow().getWorkflowName());
					if (existingWorkflow.isPresent()) {
						existingWorkflow.get().getColumns().clear();
						ptpRepository.save(existingWorkflow.get());
						wf.setId(existingWorkflow.get().getId());
						wf.getWorkflow().setId((existingWorkflow.get().getWorkflow().getId()));
						wf.setVersion(existingWorkflow.get().getVersion());
						wf.getWorkflow().setVersion(existingWorkflow.get().getWorkflow().getVersion());
					}
					wf.getWorkflow().setWorkflowStatus("Queued");
					savedWorkflows.add(ptpRepository.save(wf));

				});

		return savedWorkflows;
	}

}
