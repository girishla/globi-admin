package com.globi.infa.datasource.type.oracle;

import org.springframework.stereotype.Component;

import com.globi.infa.datasource.core.DataTypeMapper;

@Component
public class OracleInfaSourceToInfaTargetTypeMapper implements DataTypeMapper {

	
	
	public OracleInfaSourceToInfaTargetTypeMapper(){
		
	}
	
	
	@Override
	public String mapType(String fromType) {	

		//just pass through as both the source and targets are Oracle and no mapping is necessary
		return fromType;

	}

	
	
}
