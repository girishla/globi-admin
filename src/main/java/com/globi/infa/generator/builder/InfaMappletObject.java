package com.globi.infa.generator.builder;

import lombok.Getter;
import lombok.Setter;
import xjc.MAPPLET;


@Getter
@Setter
public class InfaMappletObject extends InfaFolderObject {

	public InfaMappletObject(MAPPLET mapplet){
		this.mapplet=mapplet;	
		this.folderObj=mapplet;
	
	}
	
	
	private MAPPLET mapplet;

	
}
