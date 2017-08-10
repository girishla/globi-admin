package com.globi.infa.workflow.ptp;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


@Component
public class PTPWorkflowValidator implements Validator {

	@Override
	public boolean supports(Class<?> cls) {
		return PTPWorkflow.class.equals(cls);
	}

	@Override
	public void validate(Object target, Errors errors) {
		PTPWorkflow ptpWorkflow = (PTPWorkflow) target;
		
		if(!(ptpWorkflow.getColumns().stream().anyMatch(col->col.isIntegrationIdColumn()))){
			errors.rejectValue("columns","VLD1001", "An integration Id must be specified");
			
		};

	}

}
