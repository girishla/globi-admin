package com.globi.infa.generator.service;

import com.globi.infa.AbstractInfaWorkflowEntity;

public interface GeneratorRequestAsyncProcessor extends GeneratorRequestProcessor{
	

	public void processAsync(AbstractInfaWorkflowEntity inputWorkflowDefinitions);

	
	
}
