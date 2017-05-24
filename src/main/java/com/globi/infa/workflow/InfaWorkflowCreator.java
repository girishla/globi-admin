package com.globi.infa.workflow;


public interface InfaWorkflowCreator<T> {
	
	public InfaWorkflow createWorkflow(T obj);

}
