package com.globi.infa.generator.builder;

import lombok.Getter;
import lombok.Setter;
import xjc.TARGET;


@Getter
@Setter
public class InfaTargetObject extends InfaFolderObject {

	public InfaTargetObject(TARGET target){
		this.target=target;	
		this.folderObj=target;
		this.name = target.getNAME();
		this.type = "TARGET";

	}
	
	private TARGET target;
	
}
