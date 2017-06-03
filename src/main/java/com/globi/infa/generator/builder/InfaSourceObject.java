package com.globi.infa.generator.builder;

import lombok.Getter;
import lombok.Setter;
import xjc.SOURCE;

@Getter
@Setter
public class InfaSourceObject extends InfaFolderObject {

	public InfaSourceObject(SOURCE source){
		this.source=source;	
		this.folderObj=source;

	}
	
	private SOURCE source;
	
}
