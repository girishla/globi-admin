package com.globi.infa.datasource.type.sqlserver;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.globi.infa.datasource.core.DataTypeMapper;

@Component
public class SQLServerToInfaDataTypeMapper implements DataTypeMapper {

	
	private Map<String,String>  typeMap= new HashMap<>();
	
	
	public SQLServerToInfaDataTypeMapper(){
	
		typeMap.put("number(p,s)","decimal");
		typeMap.put("char","string");
		typeMap.put("long","string");
		typeMap.put("date","date/time");
		typeMap.put("varchar2","string");
		typeMap.put("varchar","string");
	
	}
	
	
	@Override
	public String mapType(String fromType) {	

		
		return typeMap.get(fromType);

	}

	
	
}
