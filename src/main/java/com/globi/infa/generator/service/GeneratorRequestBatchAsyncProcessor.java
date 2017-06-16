package com.globi.infa.generator.service;

import java.util.List;

import com.globi.infa.AbstractInfaWorkflowEntity;

public interface GeneratorRequestBatchAsyncProcessor extends GeneratorRequestBatchProcessor{
	

	public void processAsync(List<? extends AbstractInfaWorkflowEntity> inputWorkflowDefinitions);

	
	
}
