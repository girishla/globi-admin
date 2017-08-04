package com.globi.infa.generator.sil;

import com.globi.infa.workflow.SILWorkflowSourceColumn;

public class SILStaticObjectMother {

	public static SILWorkflowSourceColumn getDimensionPKColumn(String colName) {

		return SILWorkflowSourceColumn.builder()//
				.columnName(colName)//
				.autoColumn(true)//
				.columnType("Primary Key")//
				.domainLookupColumn(false)//
				.legacyColumn(true)//
				.miniDimColumn(true)//
				.targetColumn(true)//
				.build();

	}
	
	
	public static SILWorkflowSourceColumn getDimensionAttribColumn(String colName) {

		return SILWorkflowSourceColumn.builder()//
				.columnName(colName)//
				.autoColumn(true)//
				.columnType("Attribute")//
				.domainLookupColumn(false)//
				.legacyColumn(true)//
				.miniDimColumn(true)//
				.targetColumn(true)//
				.build();

	}

}
