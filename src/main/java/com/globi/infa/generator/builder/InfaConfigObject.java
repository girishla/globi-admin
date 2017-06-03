package com.globi.infa.generator.builder;

import lombok.Getter;
import lombok.Setter;
import xjc.CONFIG;
import xjc.MAPPING;
import xjc.WORKFLOW;


@Getter
@Setter
public class InfaConfigObject extends InfaFolderObject {

	public InfaConfigObject(CONFIG config){
		this.config=config;		
		this.folderObj=config;
	}
	
	
	private CONFIG config;

	
}
