package com.globi.infa.generator.service;

import java.util.List;

import com.globi.infa.AbstractInfaWorkflowEntity;

public interface GeneratorRequestBatchProcessor {
	
	public void postProcess();
	public List<? extends AbstractInfaWorkflowEntity> buildInput();
	public List<? extends AbstractInfaWorkflowEntity> saveInput(List<? extends AbstractInfaWorkflowEntity> inputWorkflowDefinitions);
	public List<? extends AbstractInfaWorkflowEntity> process(List<? extends AbstractInfaWorkflowEntity> inputWorkflowDefinitions);

	
	
}
