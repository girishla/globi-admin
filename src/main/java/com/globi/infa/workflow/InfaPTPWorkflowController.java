package com.globi.infa.workflow;

import java.util.Optional;

import javax.validation.Valid;

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

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/workflows/ptp")
	public @ResponseBody ResponseEntity<?> createPTPExtractWorkflow(@RequestBody @Valid PTPWorkflow ptpWorkflow,
			@RequestParam("sync") Optional<Boolean> sync, BindingResult result) throws Exception {

//		if (result.hasErrors()) {
//			throw new MethodArgumentNotValidException(null, result);
//		}

		PTPWorkflow savedWorkflow = (PTPWorkflow) requestProcessor.saveInput(ptpWorkflow);

		if (sync.isPresent() && sync.get()) {
			savedWorkflow = (PTPWorkflow) requestProcessor.process(savedWorkflow);
		} else {
			requestProcessor.processAsync(savedWorkflow);
		}

		return new ResponseEntity<PTPWorkflow>(savedWorkflow, HttpStatus.CREATED);
	}

}
