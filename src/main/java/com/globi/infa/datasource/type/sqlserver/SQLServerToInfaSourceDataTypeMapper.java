package com.globi.infa.datasource.type.sqlserver;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.globi.infa.datasource.core.DataTypeMapper;
import com.globi.infa.datasource.core.DefaultHashMap;

@Component
public class SQLServerToInfaSourceDataTypeMapper implements DataTypeMapper {

	
	private Map<String,String>  typeMap= new DefaultHashMap<>("varchar2");
	
	
	SQLServerToInfaSourceDataTypeMapper(){
	
		typeMap.put("BIGINT","number(p,s)");
		typeMap.put("BIT","char");
		typeMap.put("CHAR","char");
		typeMap.put("DATE","date");
		typeMap.put("DECIMAL","number(p,s)");
		typeMap.put("DOUBLE PRECISION","number(p,s)");
		typeMap.put("DOUBLE","number(p,s)");
		typeMap.put("FLOAT","number(p,s)");
		typeMap.put("INTEGER","number(p,s)");
		typeMap.put("NUMBER","number(p,s)");
		typeMap.put("NUMERIC","number(p,s)");
		typeMap.put("REAL","number(p,s)");
		typeMap.put("ROWID","varchar2");
		typeMap.put("SMALLINT","number(p,s)");
		typeMap.put("TIMESTAMP","date");
		typeMap.put("TIMESTAMP(6)","date");
		typeMap.put("TIME","varchar2");
		typeMap.put("TINYINT","number(p,s)");
		typeMap.put("VARCHAR","varchar");
		typeMap.put("VARCHAR2","varchar2");
		typeMap.put("BLOB","blob");
		typeMap.put("CLOB","clob");
		typeMap.put("LONG","long");
	
	}
	
	
	@Override
	public String mapType(String fromType) {

		return typeMap.get(fromType);

	}

	
	
}
