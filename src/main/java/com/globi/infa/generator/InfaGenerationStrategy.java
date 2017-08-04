package com.globi.infa.generator;

import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.workflow.InfaWorkflow;

public interface InfaGenerationStrategy {

	InfaPowermartObject generate(InfaWorkflow wfInput);
	void addListener(WorkflowCreatedEventListener listener);
	
}
