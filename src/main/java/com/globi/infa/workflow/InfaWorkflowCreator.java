package com.globi.infa.workflow;


public interface InfaWorkflowCreator<T> {
	
	public T createWorkflow(T obj);

}
