package com.globi.infa.generator.builder;

import lombok.Getter;
import lombok.Setter;
import xjc.TRANSFORMATION;


@Getter
@Setter
public class InfaTransformationObject extends InfaFolderObject {

	public InfaTransformationObject(TRANSFORMATION xForm){
		this.xForm=xForm;		
		this.folderObj=xForm;

	}
	
	
	private TRANSFORMATION xForm;

	
}
