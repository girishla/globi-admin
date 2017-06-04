package com.globi.infa.generator.builder;

import lombok.Getter;
import lombok.Setter;
import xjc.CONFIG;
import xjc.MAPPING;
import xjc.SOURCE;
import xjc.WORKFLOW;


@Getter
@Setter
public class InfaWorkflowObject extends InfaFolderObject {

	public InfaWorkflowObject(WORKFLOW workflow){
		this.workflow=workflow;	
		this.folderObj=workflow;
		this.name = workflow.getNAME();
		this.type = "WORKFLOW";

	}
	
	
	private WORKFLOW workflow;

	
}
