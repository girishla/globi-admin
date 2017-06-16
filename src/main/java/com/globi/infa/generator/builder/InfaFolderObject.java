package com.globi.infa.generator.builder;

import lombok.Data;

@Data
public abstract class InfaFolderObject {

	String name;
	String type;
	
	Object folderObj;
	
	public String getUniqueName(){
		
		
		return type + "_" +name; 
		
	}
	
}
