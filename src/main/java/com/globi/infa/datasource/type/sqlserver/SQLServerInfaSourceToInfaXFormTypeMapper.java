package com.globi.infa.datasource.type.sqlserver;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.globi.infa.datasource.core.DataTypeMapper;

@Component
public class SQLServerInfaSourceToInfaXFormTypeMapper implements DataTypeMapper {

	
	private Map<String,String>  typeMap= new HashMap<>();
	
	
	public SQLServerInfaSourceToInfaXFormTypeMapper(){
	
		typeMap.put("bigint","bigint");
		typeMap.put("binary","binary");
		typeMap.put("bit","string");
		typeMap.put("char","string");
		typeMap.put("datetime","date/time");
		typeMap.put("decimal","decimal");
		typeMap.put("float","double");
		typeMap.put("image","binary");
		typeMap.put("int","integer");
		typeMap.put("money","decimal");
		typeMap.put("nchar","nstring");
		typeMap.put("ntext","ntext");
		typeMap.put("numeric","decimal");
		typeMap.put("numeric identity","integer");
		typeMap.put("nvarchar","nstring");
		typeMap.put("real","real");
		typeMap.put("smalldatetime","date/time");
		typeMap.put("smallint","small integer");
		typeMap.put("smallmoney","decimal");
		typeMap.put("sysname","nstring");
		typeMap.put("text","text");
		typeMap.put("timestamp","binary");
		typeMap.put("tinyint","string");
		typeMap.put("varbinary","binary");
		typeMap.put("varchar","string");

		
	}
	
	
	@Override
	public String mapType(String fromType) {	

		
		return typeMap.get(fromType);

	}

	
	
}
