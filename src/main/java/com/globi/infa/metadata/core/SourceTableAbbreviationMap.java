package com.globi.infa.metadata.core;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.globi.infa.datasource.core.DefaultHashMap;

@Component
public class SourceTableAbbreviationMap  {

	private Map<String, String> typeAbbrMap = new DefaultHashMap<>("AUX");

	SourceTableAbbreviationMap() {

		
		//CRM
		typeAbbrMap.put("S_ORG_EXT", "ORG");
		typeAbbrMap.put("S_PROD_INT", "PRD");
		typeAbbrMap.put("S_CONTACT", "CON");
		typeAbbrMap.put("S_DOC_AGREE", "AGR");
		typeAbbrMap.put("S_ASSET", "AST");
		typeAbbrMap.put("S_OPTY", "OPP");
		typeAbbrMap.put("S_REVN", "OPR");
		typeAbbrMap.put("S_DOC_QUOTE", "QUO");
		typeAbbrMap.put("S_BU", "BUN");
		typeAbbrMap.put("S_ORDER", "ORD");
		typeAbbrMap.put("S_PARTY", "PAR");
		typeAbbrMap.put("S_STG", "STG");
		typeAbbrMap.put("S_TIMEZONE", "TZN");
		typeAbbrMap.put("S_QUOTE_ITEM", "QIT");
		typeAbbrMap.put("S_ORDER_ITEM", "OIT");
		typeAbbrMap.put("S_USER", "USR");
		typeAbbrMap.put("S_EXCH_RATE", "XRT");
		typeAbbrMap.put("S_POSTN", "POS");
		typeAbbrMap.put("S_PERIOD", "PER");

		
		//GEN
		typeAbbrMap.put("R_INVOICE_MASTER", "INV");
		typeAbbrMap.put("R_INVOICE_LINE", "ILN");
		typeAbbrMap.put("S_HEADER", "SUB");
		typeAbbrMap.put("S_LINE", "SLN");
		

		

	}

	public String map(String fromTable) {

		return typeAbbrMap.get(fromTable);

	}

}
