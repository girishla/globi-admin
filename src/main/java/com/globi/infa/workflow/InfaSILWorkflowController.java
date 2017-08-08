package com.globi.infa.workflow;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.globi.infa.generator.service.GeneratorRequestAsyncProcessor;
import com.globi.infa.generator.sil.SILDimensionGenerationStrategy;
import com.globi.infa.generator.sil.SILFactGenerationStrategy;

@RestController
public class InfaSILWorkflowController {

	private final GeneratorRequestAsyncProcessor requestProcessor;
	

	InfaSILWorkflowController(GeneratorRequestAsyncProcessor requestProcessor) {
		this.requestProcessor = requestProcessor;

	}
	
	
	@Lookup
	public SILDimensionGenerationStrategy getSilDimensionGenerator() {
		return null; // This implementation will be overridden by dynamically
						// generated subclass
	}
	
	
	
	@Lookup
	public SILFactGenerationStrategy getSilFactGenerator() {
		return null; // This implementation will be overridden by dynamically
						// generated subclass
	}


	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/workflows/sil/dimension")
	public @ResponseBody ResponseEntity<?> createSILDimensionWorkflow(@RequestBody @Valid SILWorkflow silWorkflow,
			@RequestParam("sync") Optional<Boolean> sync, BindingResult result) throws Exception {

		
		SILDimensionGenerationStrategy silGenerator = getSilDimensionGenerator();

		SILWorkflow savedWorkflow = (SILWorkflow) requestProcessor.saveInput(silWorkflow);

		if (sync.isPresent() && sync.get()) {
			savedWorkflow = (SILWorkflow) requestProcessor.process(savedWorkflow,silGenerator);
		} else {
			requestProcessor.processAsync(savedWorkflow,silGenerator);
		}

		return new ResponseEntity<SILWorkflow>(savedWorkflow, HttpStatus.CREATED);
	}

	
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/workflows/sil/fact")
	public @ResponseBody ResponseEntity<?> createSILFactWorkflow(@RequestBody @Valid SILWorkflow silWorkflow,
			@RequestParam("sync") Optional<Boolean> sync, BindingResult result) throws Exception {

		
		SILFactGenerationStrategy silGenerator = getSilFactGenerator();

		SILWorkflow savedWorkflow = (SILWorkflow) requestProcessor.saveInput(silWorkflow);

		if (sync.isPresent() && sync.get()) {
			savedWorkflow = (SILWorkflow) requestProcessor.process(savedWorkflow,silGenerator);
		} else {
			requestProcessor.processAsync(savedWorkflow,silGenerator);
		}

		return new ResponseEntity<SILWorkflow>(savedWorkflow, HttpStatus.CREATED);
	}

	
	
}
