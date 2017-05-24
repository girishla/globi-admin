package com.globi.infa.generator.builder;

import static com.globi.infa.generator.InfagenDefaults.DEFAULT_DESCRIPTION;
import static com.globi.infa.generator.InfagenDefaults.DEFAULT_FOLDER_PERMISSION;
import static com.globi.infa.generator.InfagenDefaults.DEFAULT_VERSION;

import com.globi.infa.sourcedefinition.InfaSourceDefinition;

import xjc.FOLDER;
import xjc.INSTANCE;
import xjc.MAPPING;
import xjc.MAPPINGVARIABLE;
import xjc.REPOSITORY;
import xjc.SOURCE;
import xjc.TARGET;
import xjc.TRANSFORMATION;


/*
 * static factory methods - used when a builder might be overkill
 * */


public class RawStaticFactory {


	public static MAPPING getMappingFrom(InfaSourceDefinition sourceTable) {

		MAPPING mappingDefn = new MAPPING();

		mappingDefn.setDESCRIPTION(DEFAULT_DESCRIPTION.getValue());
		mappingDefn.setNAME("PTP_" + sourceTable.getSourceTableName() + "_Extract");
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
//		folder.setUUID(UUID.randomUUID().toString());
		folder.setUUID("AUTOGEN_UUID");
		return folder;

	}

	public static REPOSITORY getRepository() {

		REPOSITORY repository = new REPOSITORY();

		repository = new REPOSITORY();
		repository.setNAME("DEV_Oracle_BI_RS");
		repository.setVERSION("181");
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

		return instance;
	}

	public static INSTANCE getInstanceFor(SOURCE source) {

		INSTANCE instance = new INSTANCE();
		instance.setNAME(source.getNAME());
		instance.setDESCRIPTION("");
		instance.setTRANSFORMATIONTYPE("Source Definition");
		instance.setTRANSFORMATIONNAME(source.getNAME());
		instance.setTYPE("SOURCE");

		return instance;
	}

	public static INSTANCE getInstanceFor(TARGET target) {

		INSTANCE instance = new INSTANCE();

		instance.setNAME(target.getNAME());
		instance.setDESCRIPTION("");
		instance.setTRANSFORMATIONTYPE("Target Definition");
		instance.setTRANSFORMATIONNAME(target.getNAME());
		instance.setTYPE("TARGET");
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
		mappingVariable.setNAME("$$ETL_PROC_WID");
		mappingVariable.setPRECISION("10");
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
