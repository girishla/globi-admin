package com.globi.infa.workflow;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.globi.infa.generator.ptp.PTPGenerationStrategy;
import com.globi.infa.generator.service.GeneratorRequestAsyncProcessor;

@RestController
public class InfaPTPWorkflowController {

	private final GeneratorRequestAsyncProcessor requestProcessor;

	private PTPWorkflowValidator validator;
	

	InfaPTPWorkflowController(GeneratorRequestAsyncProcessor requestProcessor, PTPWorkflowValidator validator) {
		this.requestProcessor = requestProcessor;
		this.validator = validator;

	}

	@InitBinder
	protected void initBinder(final WebDataBinder binder) {
		binder.addValidators(validator);
	}
	
	
	@Lookup
	//Get an instance of the correct service to use. Returns a dynamically created bean for each request,.
	public PTPGenerationStrategy getPtpGenerator() {
		return null; // This implementation will be overridden by dynamically
						// generated subclass
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/workflows/ptp")
	public @ResponseBody ResponseEntity<?> createPTPExtractWorkflow(@RequestBody @Valid PTPWorkflow ptpWorkflow,
			@RequestParam("sync") Optional<Boolean> sync, BindingResult result) throws Exception {

		
		PTPGenerationStrategy ptpGenerator = getPtpGenerator();

		PTPWorkflow savedWorkflow = (PTPWorkflow) requestProcessor.saveInput(ptpWorkflow);

		if (sync.isPresent() && sync.get()) {
			savedWorkflow = (PTPWorkflow) requestProcessor.process(savedWorkflow,ptpGenerator);
		} else {
			requestProcessor.processAsync(savedWorkflow,ptpGenerator);
		}

		return new ResponseEntity<PTPWorkflow>(savedWorkflow, HttpStatus.CREATED);
	}

}
