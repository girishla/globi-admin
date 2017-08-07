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
	
	public static SILWorkflowSourceColumn getSpecialColumn(String colName,String type) {

		return SILWorkflowSourceColumn.builder()//
				.columnName(colName)//
				.autoColumn(true)//
				.columnType(type)//
				.domainLookupColumn(false)//
				.legacyColumn(true)//
				.miniDimColumn(true)//
				.targetColumn(false)//
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
	
	
	public static SILWorkflowSourceColumn getMeasureAttribColumn(String colName) {

		return SILWorkflowSourceColumn.builder()//
				.columnName(colName)//
				.autoColumn(false)//
				.columnType("Measure Attribute")//
				.domainLookupColumn(false)//
				.legacyColumn(false)//
				.miniDimColumn(false)//
				.stageTableColumn(true)
				.targetColumn(true)//
				.build();

	}
	
	
	public static SILWorkflowSourceColumn getMeasureColumn(String colName) {

		return SILWorkflowSourceColumn.builder()//
				.columnName(colName)//
				.autoColumn(false)//
				.columnType("Measure")//
				.domainLookupColumn(false)//
				.legacyColumn(false)//
				.miniDimColumn(false)//
				.stageTableColumn(true)
				.targetColumn(true)//
				.build();

	}

	
	public static SILWorkflowSourceColumn getFKWIDColumn(String colName,String dimTable) {

		return SILWorkflowSourceColumn.builder()//
				.columnName(colName)//
				.autoColumn(false)//
				.columnType("Foreign Key")//
				.domainLookupColumn(false)//
				.legacyColumn(false)//
				.miniDimColumn(false)//
				.stageTableColumn(false)
				.targetColumn(true)//
				.columnDimTable(dimTable)
				.build();

	}
	
}
