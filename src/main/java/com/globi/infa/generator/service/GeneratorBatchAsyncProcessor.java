package com.globi.infa.generator.service;

import java.util.List;

import com.globi.infa.AbstractInfaWorkflowEntity;

public interface GeneratorBatchAsyncProcessor extends GeneratorBatchProcessor{
	

	public void processAsync(List<? extends AbstractInfaWorkflowEntity> inputWorkflowDefinitions);

	
	
}
