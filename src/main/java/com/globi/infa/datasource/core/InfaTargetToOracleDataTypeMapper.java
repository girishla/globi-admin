package com.globi.infa.datasource.core;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class InfaTargetToOracleDataTypeMapper implements DataTypeMapper {

	private Map<String, String> typeMap = new DefaultHashMap<>("varchar2");

	InfaTargetToOracleDataTypeMapper() {

		typeMap.put("number(p,s)", "NUMBER");
		typeMap.put("char", "VARCHAR2");
		typeMap.put("date", "DATE");
		typeMap.put("varchar2", "VARCHAR2");
		typeMap.put("blob", "BLOB");
		typeMap.put("clob", "CLOB");
		typeMap.put("long", "LONG");

	}

	@Override
	public String mapType(String fromType) {

		return typeMap.get(fromType);

	}

}
