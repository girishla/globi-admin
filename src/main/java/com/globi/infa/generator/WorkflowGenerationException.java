package com.globi.infa.generator;

import org.springframework.util.Assert;

import com.globi.infa.workflow.GeneratedWorkflow;

import lombok.Getter;

@Getter
public class WorkflowGenerationException extends RuntimeException {

	private static final long serialVersionUID = -4929826142920520541L;
	private final GeneratedWorkflow wf;

	public WorkflowGenerationException(GeneratedWorkflow wfDefinition, String message) {

		super(message);

		Assert.notNull(wfDefinition, "workflow definition must not be null");
		this.wf = wfDefinition;
	}
}
