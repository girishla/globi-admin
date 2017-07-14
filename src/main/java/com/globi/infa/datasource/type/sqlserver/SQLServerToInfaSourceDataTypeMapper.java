package com.globi.infa.datasource.type.sqlserver;

import org.springframework.stereotype.Component;

import com.globi.infa.datasource.core.DataTypeMapper;

@Component
public class SQLServerToInfaSourceDataTypeMapper implements DataTypeMapper {

	
	
	SQLServerToInfaSourceDataTypeMapper(){
	
	
	}
	
	
	@Override
	public String mapType(String fromType) {
		//just pass through because SQL server data types seem to have exact equivalents when imported as Infa sources...unlike Oracle
		return fromType;

	}

	
	
}
