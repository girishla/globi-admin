package com.globi.infa.datasource.core;

public class ObjectNameNormaliser {

	
	public static String normalise(String name){
		
		String specialChars = "?#";
		if (specialChars.indexOf(name.charAt(0)) >= 0){
			name=name.substring(1);
		}
		name= name.replace(" ", "_");
		return name;

	}
	
}
