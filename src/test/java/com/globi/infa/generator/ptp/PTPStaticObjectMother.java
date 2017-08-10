package com.globi.infa.generator.ptp;

import static com.globi.infa.generator.InfaGeneratorDefaults.DEFAULT_DESCRIPTION;

import java.util.ArrayList;
import java.util.List;

import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.workflow.ptp.PTPWorkflowSourceColumn;

import xjc.SOURCE;
import xjc.SOURCEFIELD;
import xjc.TARGETFIELD;

public class PTPStaticObjectMother {

	public static PTPWorkflowSourceColumn getIntegrationIdColumn(String colName) {

		return PTPWorkflowSourceColumn.builder()//
				.integrationIdColumn(true)//
				.changeCaptureColumn(false)//
				.pguidColumn(false)//
				.sourceColumnName(colName)//
				.buidColumn(false)//
				.build();

	}
	
	public static PTPWorkflowSourceColumn getIntegrationIdAndPguidColumn(String colName) {

		return PTPWorkflowSourceColumn.builder()//
				.integrationIdColumn(true)//
				.changeCaptureColumn(false)//
				.pguidColumn(true)//
				.sourceColumnName(colName)//
				.buidColumn(false)//
				.build();

	}

	public static PTPWorkflowSourceColumn getCCColumn(String colName) {

		return PTPWorkflowSourceColumn.builder()//
				.integrationIdColumn(false)//
				.changeCaptureColumn(true)//
				.pguidColumn(false)//
				.sourceColumnName(colName)//
				.buidColumn(false)//
				.build();

	}
	
	
	public static PTPWorkflowSourceColumn getPguidColumn(String colName) {

		return PTPWorkflowSourceColumn.builder()//
				.integrationIdColumn(false)//
				.changeCaptureColumn(false)//
				.pguidColumn(true)//
				.sourceColumnName(colName)//
				.buidColumn(false)//
				.build();

	}

	public static PTPWorkflowSourceColumn getNormalColumn(String colName) {

		return PTPWorkflowSourceColumn.builder()//
				.integrationIdColumn(false)//
				.changeCaptureColumn(false)//
				.pguidColumn(false)//
				.sourceColumnName(colName)//
				.buidColumn(false)//
				.build();

	}

	public static PTPWorkflowSourceColumn getBuidColumn(String colName) {

		return PTPWorkflowSourceColumn.builder()//
				.integrationIdColumn(false)//
				.changeCaptureColumn(false)//
				.pguidColumn(false)//
				.sourceColumnName(colName)//
				.buidColumn(true)//
				.build();

	}

	public static TARGETFIELD targetVarcharField(String fieldName, Integer length) {

		TARGETFIELD targetField = new TARGETFIELD();

		targetField.setBUSINESSNAME(DEFAULT_DESCRIPTION.getValue());
		targetField.setDATATYPE("varchar2");
		targetField.setFIELDNUMBER(Integer.toString(0));
		targetField.setNULLABLE("NULL");
		targetField.setNAME(fieldName);
		targetField.setKEYTYPE("NOT A KEY");
		targetField.setPICTURETEXT("");
		targetField.setPRECISION(Integer.toString(length));
		targetField.setSCALE("0");

		return targetField;

	}

	public static TARGETFIELD targetNumberField(String fieldName) {

		TARGETFIELD targetField = new TARGETFIELD();

		targetField.setBUSINESSNAME(DEFAULT_DESCRIPTION.getValue());
		targetField.setDATATYPE("number(p,s)");
		targetField.setFIELDNUMBER(Integer.toString(0));
		targetField.setNULLABLE("NULL");
		targetField.setNAME(fieldName);
		targetField.setKEYTYPE("NOT A KEY");
		targetField.setPICTURETEXT("");
		targetField.setPRECISION("22");
		targetField.setSCALE("7");

		return targetField;

	}

	public static TARGETFIELD targetDateField(String fieldName) {

		TARGETFIELD targetField = new TARGETFIELD();

		targetField.setBUSINESSNAME(DEFAULT_DESCRIPTION.getValue());
		targetField.setDATATYPE("date");
		targetField.setFIELDNUMBER(Integer.toString(0));
		targetField.setNULLABLE("NULL");
		targetField.setNAME(fieldName);
		targetField.setKEYTYPE("NOT A KEY");
		targetField.setPICTURETEXT("");
		targetField.setPRECISION("19");
		targetField.setSCALE("0");

		return targetField;

	}

	
}
