package com.globi.infa.generator;

import com.globi.infa.workflow.PTPWorkflowSourceColumn;

public class StaticObjectMother {

	public static PTPWorkflowSourceColumn getIntegrationIdColumn(String colName){
		
		return PTPWorkflowSourceColumn.builder()//
				.integrationIdColumn(true)//
				.changeCaptureColumn(false)//
				.pguidColumn(true)//
				.sourceColumnName(colName)//
				.buidColumn(false)//
				.build();
		
	}
	
	
	public static PTPWorkflowSourceColumn getCCColumn(String colName){
		
		return PTPWorkflowSourceColumn.builder()//
				.integrationIdColumn(false)//
				.changeCaptureColumn(true)//
				.pguidColumn(false)//
				.sourceColumnName(colName)//
				.buidColumn(false)//
				.build();
		
	}
	
	public static PTPWorkflowSourceColumn getNormalColumn(String colName){
		
		return PTPWorkflowSourceColumn.builder()//
				.integrationIdColumn(false)//
				.changeCaptureColumn(false)//
				.pguidColumn(false)//
				.sourceColumnName(colName)//
				.buidColumn(false)//
				.build();
		
	}
	
	public static PTPWorkflowSourceColumn getBuidColumn(String colName){
		
		return PTPWorkflowSourceColumn.builder()//
				.integrationIdColumn(false)//
				.changeCaptureColumn(false)//
				.pguidColumn(false)//
				.sourceColumnName(colName)//
				.buidColumn(true)//
				.build();
		
	}
	
	
}
