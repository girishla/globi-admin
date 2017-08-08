package com.globi.infa.generator.builder;

import static com.globi.infa.generator.InfaGeneratorDefaults.DEFAULT_DESCRIPTION;
import static com.globi.infa.generator.InfaGeneratorDefaults.DEFAULT_FOLDER_PERMISSION;
import static com.globi.infa.generator.InfaGeneratorDefaults.DEFAULT_VERSION;

import com.globi.infa.metadata.src.InfaSourceDefinition;

import xjc.FOLDER;
import xjc.INSTANCE;
import xjc.MAPPING;
import xjc.MAPPINGVARIABLE;
import xjc.MAPPLET;
import xjc.REPOSITORY;
import xjc.SOURCE;
import xjc.TABLEATTRIBUTE;
import xjc.TARGET;
import xjc.TRANSFORMATION;


/*
 * static factory methods - used when a builder might be overkill
 * */


public class InfaObjectMother {


	public static MAPPING getMappingFrom(String mappingName) {

		MAPPING mappingDefn = new MAPPING();

		mappingDefn.setDESCRIPTION(DEFAULT_DESCRIPTION.getValue());
		mappingDefn.setNAME(mappingName);
		mappingDefn.setOBJECTVERSION(DEFAULT_VERSION.getValue());
		mappingDefn.setVERSIONNUMBER(DEFAULT_VERSION.getValue());
		mappingDefn.setISVALID("YES");

		return mappingDefn;

	}

	public static FOLDER getFolderFor(String folderName, String folderDescription) {

		FOLDER folder = new FOLDER();
		folder.setDESCRIPTION(folderDescription);
		folder.setNAME(folderName);
		folder.setOWNER("Administrator");
		folder.setSHARED("SHARED");
		folder.setPERMISSIONS(DEFAULT_FOLDER_PERMISSION.getValue());
		folder.setUUID("AUTOGEN_UUID");
		return folder;

	}

	public static REPOSITORY getRepository() {

		REPOSITORY repository = new REPOSITORY();

		repository = new REPOSITORY();
		repository.setNAME("GLOBI_RS_DEV1");
		repository.setVERSION("186");
		repository.setDATABASETYPE("Oracle");
		repository.setCODEPAGE("UTF-8");

		return repository;
	}

	public static INSTANCE getInstanceFor(TRANSFORMATION xform) {

		INSTANCE instance = new INSTANCE();

		instance.setNAME(xform.getNAME());
		instance.setDESCRIPTION(xform.getDESCRIPTION());
		instance.setTRANSFORMATIONTYPE(xform.getTYPE());
		instance.setTRANSFORMATIONNAME(xform.getNAME());
		instance.setTYPE("TRANSFORMATION");
		instance.setREUSABLE("NO");

		return instance;
	}

	public static INSTANCE getInstanceFor(SOURCE source) {

		INSTANCE instance = new INSTANCE();
		instance.setNAME(source.getNAME());
		instance.setDESCRIPTION("");
		instance.setTRANSFORMATIONTYPE("Source Definition");
		instance.setTRANSFORMATIONNAME(source.getNAME());
		instance.setTYPE("SOURCE");
		instance.setDBDNAME(source.getDBDNAME());
		
		TABLEATTRIBUTE ta=new TABLEATTRIBUTE();
		ta.setNAME("Source Table Name");
		ta.setVALUE(source.getOWNERNAME() + "." + source.getNAME());
		instance.getTABLEATTRIBUTE().add(ta);

		return instance;
	}
	
	public static INSTANCE getInstanceFor(MAPPLET mapplet) {

		INSTANCE instance = new INSTANCE();
		instance.setNAME(mapplet.getNAME());
		instance.setDESCRIPTION("");
		instance.setTRANSFORMATIONTYPE("Mapplet");
		instance.setTRANSFORMATIONNAME(mapplet.getNAME());
		instance.setTYPE("MAPPLET");
		instance.setREUSABLE("YES");

		return instance;
	}


	public static INSTANCE getInstanceFor(TARGET target) {

		INSTANCE instance = new INSTANCE();

		instance.setNAME(target.getNAME());
		instance.setDESCRIPTION("");
		instance.setTRANSFORMATIONTYPE("Target Definition");
		instance.setTRANSFORMATIONNAME(target.getNAME());
		instance.setTYPE("TARGET");
		
		TABLEATTRIBUTE ta=new TABLEATTRIBUTE();
		ta.setNAME("Pre SQL");
		ta.setVALUE("TRUNCATE TABLE "  + target.getNAME());
		instance.getTABLEATTRIBUTE().add(ta);
		
		return instance;
	}


	public static TRANSFORMATION getExprTransform(String name) {

		TRANSFORMATION exprXform = new TRANSFORMATION();
		exprXform.setNAME(name);
		exprXform.setTYPE("Expression");
		exprXform.setREUSABLE("NO");
		exprXform.setDESCRIPTION("");
		exprXform.setOBJECTVERSION(DEFAULT_VERSION.getValue());
		exprXform.setVERSIONNUMBER(DEFAULT_VERSION.getValue());
		return exprXform;

	}



	public static MAPPINGVARIABLE getEtlProcWidMappingVariable() {

		MAPPINGVARIABLE mappingVariable = new MAPPINGVARIABLE();
		mappingVariable.setDATATYPE("decimal");
		mappingVariable.setDEFAULTVALUE("0");
		mappingVariable.setDESCRIPTION("");
		mappingVariable.setISEXPRESSIONVARIABLE("NO");
		mappingVariable.setISPARAM("YES");
		mappingVariable.setNAME("$$SYS_ETL_PROC_WID");
		mappingVariable.setPRECISION("10");
		mappingVariable.setSCALE("0");
		mappingVariable.setUSERDEFINED("YES");
		return mappingVariable;

	}
	
	
	public static MAPPINGVARIABLE getTargetTableMappingVariable(String tableName) {

		MAPPINGVARIABLE mappingVariable = new MAPPINGVARIABLE();
		mappingVariable.setDATATYPE("string");
		mappingVariable.setDEFAULTVALUE(tableName);
		mappingVariable.setDESCRIPTION("");
		mappingVariable.setISEXPRESSIONVARIABLE("NO");
		mappingVariable.setISPARAM("YES");
		mappingVariable.setNAME("$$TABLE_TARGET");
		mappingVariable.setPRECISION("15");
		mappingVariable.setSCALE("0");
		mappingVariable.setUSERDEFINED("YES");
		return mappingVariable;

	}

	
	
	public static MAPPINGVARIABLE getUnspecifiedStringMappingVariable() {

		MAPPINGVARIABLE mappingVariable = new MAPPINGVARIABLE();
		mappingVariable.setDATATYPE("string");
		mappingVariable.setDEFAULTVALUE("Unspecified");
		mappingVariable.setDESCRIPTION("");
		mappingVariable.setISEXPRESSIONVARIABLE("NO");
		mappingVariable.setISPARAM("YES");
		mappingVariable.setNAME("$$UNSPEC_STR");
		mappingVariable.setPRECISION("15");
		mappingVariable.setSCALE("0");
		mappingVariable.setUSERDEFINED("YES");
		return mappingVariable;

	}


	
	public static MAPPINGVARIABLE getUnspecifiedFlagMappingVariable() {

		MAPPINGVARIABLE mappingVariable = new MAPPINGVARIABLE();
		mappingVariable.setDATATYPE("string");
		mappingVariable.setDEFAULTVALUE("U");
		mappingVariable.setDESCRIPTION("");
		mappingVariable.setISEXPRESSIONVARIABLE("NO");
		mappingVariable.setISPARAM("YES");
		mappingVariable.setNAME("$$UNSPEC_FLG");
		mappingVariable.setPRECISION("1");
		mappingVariable.setSCALE("0");
		mappingVariable.setUSERDEFINED("YES");
		return mappingVariable;

	}

	
	public static MAPPINGVARIABLE getUnspecifiedNumMappingVariable() {

		MAPPINGVARIABLE mappingVariable = new MAPPINGVARIABLE();
		mappingVariable.setDATATYPE("decimal");
		mappingVariable.setDEFAULTVALUE("0");
		mappingVariable.setDESCRIPTION("");
		mappingVariable.setISEXPRESSIONVARIABLE("NO");
		mappingVariable.setISPARAM("YES");
		mappingVariable.setNAME("$$UNSPEC_NUM");
		mappingVariable.setPRECISION("10");
		mappingVariable.setSCALE("0");
		mappingVariable.setUSERDEFINED("YES");
		return mappingVariable;

	}
	
	
	public static MAPPINGVARIABLE getUnspecifiedDateMappingVariable() {

		MAPPINGVARIABLE mappingVariable = new MAPPINGVARIABLE();
		mappingVariable.setDATATYPE("date/time");
		mappingVariable.setDEFAULTVALUE("01/01/1900");
		mappingVariable.setDESCRIPTION("");
		mappingVariable.setISEXPRESSIONVARIABLE("NO");
		mappingVariable.setISPARAM("YES");
		mappingVariable.setNAME("$$UNSPEC_DT");
		mappingVariable.setPRECISION("29");
		mappingVariable.setSCALE("9");
		mappingVariable.setUSERDEFINED("YES");
		return mappingVariable;

	}

	
	
	public static MAPPINGVARIABLE getDataSourceNumIdMappingVariable(String defaultValue) {

		MAPPINGVARIABLE mappingVariable = new MAPPINGVARIABLE();
		mappingVariable.setDATATYPE("decimal");
		mappingVariable.setDEFAULTVALUE(defaultValue);
		mappingVariable.setDESCRIPTION("");
		mappingVariable.setISEXPRESSIONVARIABLE("NO");
		mappingVariable.setISPARAM("YES");
		mappingVariable.setNAME("$$SYS_DATASOURCE_NUM_ID");
		mappingVariable.setPRECISION("10");
		mappingVariable.setSCALE("0");
		mappingVariable.setUSERDEFINED("YES");
		return mappingVariable;

	}
	
	
	public static MAPPINGVARIABLE getTablenameMappingVariable(String tableName) {

		MAPPINGVARIABLE mappingVariable = new MAPPINGVARIABLE();
		mappingVariable.setDATATYPE("string");
		mappingVariable.setDEFAULTVALUE(tableName);
		mappingVariable.setDESCRIPTION("");
		mappingVariable.setISEXPRESSIONVARIABLE("NO");
		mappingVariable.setISPARAM("YES");
		mappingVariable.setNAME("$$TABLE_NAME");
		mappingVariable.setPRECISION("50");
		mappingVariable.setSCALE("0");
		mappingVariable.setUSERDEFINED("YES");
		return mappingVariable;

	}

	public static MAPPINGVARIABLE getInitialExtractDateMappingVariable() {

		MAPPINGVARIABLE mappingVariable = new MAPPINGVARIABLE();
		mappingVariable.setDATATYPE("date/time");
		mappingVariable.setDEFAULTVALUE("01/01/2014");
		mappingVariable.setDESCRIPTION("");
		mappingVariable.setISEXPRESSIONVARIABLE("NO");
		mappingVariable.setISPARAM("YES");
		mappingVariable.setNAME("$$INITIAL_EXTRACT_DATE");
		mappingVariable.setPRECISION("29");
		mappingVariable.setSCALE("9");
		mappingVariable.setUSERDEFINED("YES");
		return mappingVariable;

	}

}
