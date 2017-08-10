package com.globi.infa.workflow.sil;

import java.util.ArrayList;
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
import com.globi.infa.metadata.sil.TopDownSILTableDefn;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class InfaSILWorkflowFromMetadataController {

	private final GeneratorBatchAsyncProcessor requestProcessor;

	InfaSILWorkflowFromMetadataController(@Qualifier("SILBatchProcessor") GeneratorBatchAsyncProcessor requestProcessor) {
		this.requestProcessor = requestProcessor;

	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/workflows/silFromMetadata")
	public @ResponseBody ResponseEntity<?> createSILExtractWorkflow(@RequestParam("sync") Optional<Boolean> sync,
			@RequestBody @Valid List<TopDownSILTableDefn> tables) {

		List<AbstractInfaWorkflowEntity> inputDefinitions=new ArrayList<>();
		List<SILWorkflow> responseWorkflows;
		
		for(TopDownSILTableDefn table:tables){
			inputDefinitions.add(requestProcessor.buildInputFor(table.getLoadType(),table.getTableName()));
		}
		
		List<? extends AbstractInfaWorkflowEntity> savedWorkflowDefinitions = requestProcessor
				.saveInput(inputDefinitions);

		if (sync.isPresent() && sync.get()) {
			responseWorkflows = requestProcessor.process(savedWorkflowDefinitions).stream()//
					.map(wf -> (SILWorkflow) wf)//
					.collect(Collectors.toList());
		} else {
			requestProcessor.processAsync(savedWorkflowDefinitions);
			responseWorkflows = savedWorkflowDefinitions.stream()//
					.map(wf -> (SILWorkflow) wf)//
					.collect(Collectors.toList());
		}

		return new ResponseEntity<List<SILWorkflow>>(responseWorkflows, HttpStatus.CREATED);

		
	}

}
