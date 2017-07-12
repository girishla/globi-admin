package com.globi.infa.datasource.core;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class InfaXformToInfaTargetDataTypeMapper implements DataTypeMapper {

	private Map<String, String> typeMap = new DefaultHashMap<>("varchar2");

	InfaXformToInfaTargetDataTypeMapper() {

		typeMap.put("bigint","number(p,s)");
		typeMap.put("binary","raw");
		typeMap.put("string","varchar2");
		typeMap.put("date/time","date");
		typeMap.put("decimal","number(p,s)");
		typeMap.put("double","number(p,s)");
		typeMap.put("integer","number(p,s)");
		typeMap.put("nstring","varchar2");
		typeMap.put("ntext","varchar2");
		typeMap.put("real","number(p,s)");
		typeMap.put("small integer","number(p,s)");
		typeMap.put("text","varchar2");


	}

	@Override
	public String mapType(String fromType) {

		return typeMap.get(fromType);

	}

}
