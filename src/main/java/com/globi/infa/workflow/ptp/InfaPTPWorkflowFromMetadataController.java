package com.globi.infa.workflow.ptp;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.globi.infa.AbstractInfaWorkflowEntity;
import com.globi.infa.generator.service.GeneratorBatchAsyncProcessor;
import com.globi.infa.metadata.ptp.TopDownPTPTableDefn;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class InfaPTPWorkflowFromMetadataController {

	
	private final GeneratorBatchAsyncProcessor requestProcessor;

	InfaPTPWorkflowFromMetadataController(@Qualifier("PTPBatchProcessor") GeneratorBatchAsyncProcessor requestProcessor) {
		this.requestProcessor = requestProcessor;

	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/workflows/ptpFromMetadata")
	public @ResponseBody ResponseEntity<?> createPTPExtractWorkflow(@RequestParam("sync") Optional<Boolean> sync,
			@RequestBody @Valid Optional<List<TopDownPTPTableDefn>> tables) {

		List<? extends AbstractInfaWorkflowEntity> inputDefinitions = requestProcessor.buildInput();

		if(tables.isPresent()){
			//Filter if any specific tables are provided
			inputDefinitions=inputDefinitions.stream()
					.filter(inputWF -> tables.get().stream()//
							.anyMatch(table -> (((PTPWorkflow) inputWF).getSourceTableName().equals(table.getTableName()))
									&& ((PTPWorkflow) inputWF).getSourceName().equals(table.getSource())))
					.collect(Collectors.toList());	
		}

		List<? extends AbstractInfaWorkflowEntity> savedWorkflowDefinitions = requestProcessor
				.saveInput(inputDefinitions);
		List<PTPWorkflow> responseWorkflows;

		if (sync.isPresent() && sync.get()) {
			responseWorkflows = requestProcessor.process(savedWorkflowDefinitions).stream()//
					.map(wf -> (PTPWorkflow) wf)//
					.collect(Collectors.toList());
		} else {
			requestProcessor.processAsync(savedWorkflowDefinitions);
			responseWorkflows = savedWorkflowDefinitions.stream()//
					.map(wf -> (PTPWorkflow) wf)//
					.collect(Collectors.toList());
		}

		return new ResponseEntity<List<PTPWorkflow>>(responseWorkflows, HttpStatus.CREATED);

	}

}
