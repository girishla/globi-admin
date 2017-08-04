package com.globi.infa.generator.service;

import com.globi.infa.AbstractInfaWorkflowEntity;
import com.globi.infa.generator.InfaGenerationStrategy;

public interface GeneratorRequestAsyncProcessor extends GeneratorRequestProcessor{
	

	public void processAsync(AbstractInfaWorkflowEntity inputWorkflowDefinitions,InfaGenerationStrategy generator);

	
	
}
