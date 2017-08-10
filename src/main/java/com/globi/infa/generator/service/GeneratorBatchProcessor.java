package com.globi.infa.generator.service;

import java.util.List;

import com.globi.infa.AbstractInfaWorkflowEntity;

public interface GeneratorBatchProcessor {
	
	public void postProcess();
	public List<? extends AbstractInfaWorkflowEntity> buildInput();
	public AbstractInfaWorkflowEntity buildInputFor(String processType,String itemName);
	public List<? extends AbstractInfaWorkflowEntity> saveInput(List<? extends AbstractInfaWorkflowEntity> inputWorkflowDefinitions);
	public List<? extends AbstractInfaWorkflowEntity> process(List<? extends AbstractInfaWorkflowEntity> inputWorkflowDefinitions);

	
	
}
