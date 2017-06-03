package com.globi.infa.generator.builder;

import lombok.Getter;
import lombok.Setter;
import xjc.MAPPING;


@Getter
@Setter
public class InfaMappingObject extends InfaFolderObject {

	public InfaMappingObject(MAPPING mapping){
		this.mapping=mapping;	
		this.folderObj=mapping;
	}
	
	
	private MAPPING mapping;

	
}
