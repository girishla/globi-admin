package com.globi.infa.datasource.type.oracle;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.globi.infa.datasource.core.DataTypeMapper;

@Component
public class OracleInfaSourceToInfaXFormTypeMapper implements DataTypeMapper {

	
	private Map<String,String>  typeMap= new HashMap<>();
	
	
	public OracleInfaSourceToInfaXFormTypeMapper(){
	
		typeMap.put("number(p,s)","decimal");
		typeMap.put("char","string");
		typeMap.put("long","string");
		typeMap.put("date","date/time");
		typeMap.put("varchar2","string");
		typeMap.put("varchar","string");
		typeMap.put("clob","string");
		
	
	}
	
	
	@Override
	public String mapType(String fromType) {

		
		return typeMap.get(fromType);

	}

	
	
}
