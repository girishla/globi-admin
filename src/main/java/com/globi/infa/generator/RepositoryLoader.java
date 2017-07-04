package com.globi.infa.generator;

import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.workflow.GeneratedWorkflow;

public interface RepositoryLoader {
	public void loadWorkflow(InfaPowermartObject generatedObject, GeneratedWorkflow wf);
}
