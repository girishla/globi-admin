package com.globi.infa.generator.builder;

import lombok.Getter;
import lombok.Setter;
import xjc.CONFIG;


@Getter
@Setter
public class InfaConfigObject extends InfaFolderObject {

	public InfaConfigObject(CONFIG config){
		this.config=config;		
		this.folderObj=config;
		this.name = config.getNAME();
		this.type = "CONFIG";
	}
	
	
	private CONFIG config;

	
}
