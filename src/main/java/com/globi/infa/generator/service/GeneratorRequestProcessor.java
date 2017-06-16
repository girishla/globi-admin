package com.globi.infa.generator.service;

import com.globi.infa.AbstractInfaWorkflowEntity;

public interface GeneratorRequestProcessor {
	
	public void postProcess();
	public AbstractInfaWorkflowEntity saveInput(AbstractInfaWorkflowEntity inputWorkflowDefinitions);
	public AbstractInfaWorkflowEntity process(AbstractInfaWorkflowEntity inputWorkflowDefinitions);

	
	
}
