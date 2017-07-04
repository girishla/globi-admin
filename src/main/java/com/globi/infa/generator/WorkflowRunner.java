package com.globi.infa.generator;

import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.workflow.GeneratedWorkflow;

public interface WorkflowRunner {

	void run(InfaPowermartObject generatedObject,GeneratedWorkflow wf);

}
