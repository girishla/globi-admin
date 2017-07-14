package com.globi.infa.datasource.type.sqlserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.globi.infa.datasource.core.DataTypeMapper;
import com.globi.infa.datasource.core.InfaXformToInfaTargetDataTypeMapper;

@Component
public class SQLServerInfaSourceToInfaTargetTypeMapper implements DataTypeMapper {


	@Autowired
	private InfaXformToInfaTargetDataTypeMapper toTarget;
	
	@Autowired
	private SQLServerInfaSourceToInfaXFormTypeMapper toXform;
	
	
	public SQLServerInfaSourceToInfaTargetTypeMapper(){
	


		
	}
	
	
	@Override
	public String mapType(String fromType) {	

		return toTarget.mapType(toXform.mapType(fromType));

	}

	
	
}
