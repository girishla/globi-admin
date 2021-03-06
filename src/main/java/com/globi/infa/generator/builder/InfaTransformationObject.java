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
		this.name = xForm.getNAME();
		this.type = "TRANSFORMATION";
	}
	
	
	private TRANSFORMATION xForm;

	
}
