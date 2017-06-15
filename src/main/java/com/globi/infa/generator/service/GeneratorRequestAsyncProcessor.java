package com.globi.infa.generator.service;

import com.globi.infa.AbstractInfaWorkflowEntity;

public interface GeneratorRequestAsyncProcessor {
	
	public void postProcess();
	public AbstractInfaWorkflowEntity saveInput(AbstractInfaWorkflowEntity inputWorkflowDefinitions);
	public void process(AbstractInfaWorkflowEntity inputWorkflowDefinitions);

	
	
}
