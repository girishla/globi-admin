package com.globi.infa.generator.builder;

import static com.globi.infa.generator.InfaGeneratorDefaults.DEFAULT_VERSION;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.util.FileCopyUtils;

import com.globi.infa.datasource.core.InfaSourceColumnDefinition;

import xjc.TABLEATTRIBUTE;
import xjc.TRANSFORMATION;
import xjc.TRANSFORMFIELD;

public class ExpressionXformBuilder {

	public static ClassStep newBuilder() {
		return new ExpressionXformSteps();
	}

	public interface ClassStep {
		ExpressionStep expressionFromPrototype(String className);

		SetMarshallerStep ExpressionFromSeed(String className);
	}

	public interface ExpressionStep {
		AddFieldsStep expression(String expressionName);
	}

	public interface SetMarshallerStep {
		SetInterPolationValues marshaller(Jaxb2Marshaller marshaller);
	}

	public interface SetInterPolationValues {
		LoadFromSeedStep setInterpolationValues(Map<String, String> values);

	}

	public interface LoadFromSeedStep {
		AddFieldsStep loadExpressionXformFromSeed(String seedName) throws FileNotFoundException, IOException;
	}

	public interface AddFieldsStep {
		AddFieldsStep addFields(List<InfaSourceColumnDefinition> columns);

		AddFieldsStep addRowWidField();

		AddFieldsStep addEtlProcWidField();

		AddFieldsStep addEffectiveFromDateField();

		AddFieldsStep addIntegrationIdField(List<InfaSourceColumnDefinition> columns);
		
		AddFieldsStep addDatasourceNumIdField();

		AddFieldsStep addPGUIDField(String sourceName, List<InfaSourceColumnDefinition> columns);

		AddFieldsStep addMD5HashField(List<InfaSourceColumnDefinition> columns);

		NameStep noMoreFields();

	}

	public interface NameStep {
		BuildStep name(String name);

		BuildStep nameAlreadySet();
	}

	public interface BuildStep {
		TRANSFORMATION build();
	}

	public static class ExpressionXformSteps implements ClassStep, ExpressionStep, NameStep, SetMarshallerStep,
			SetInterPolationValues, LoadFromSeedStep, AddFieldsStep, BuildStep {

		private Jaxb2Marshaller marshaller;
		private TRANSFORMATION expressionXformDefn;
		private Map<String, String> interpolationValues;

		@SuppressWarnings("unused")
		private String className;

		@Override
		public TRANSFORMATION build() {

			return this.expressionXformDefn;
		}

		@Override
		public AddFieldsStep loadExpressionXformFromSeed(String seedName) throws FileNotFoundException, IOException {

			FileInputStream is = null;

			try {
				Resource resource = new ClassPathResource("seed/" + seedName + ".xml");
				is = new FileInputStream(resource.getFile());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				FileCopyUtils.copy(is, baos);
				StrSubstitutor sub = new StrSubstitutor(interpolationValues, "{{", "}}");

				this.expressionXformDefn = (TRANSFORMATION) marshaller
						.unmarshal(new StreamSource(new StringReader(sub.replace(baos.toString("UTF-8")))));
			} finally {
				if (is != null) {
					is.close();
				}
			}

			return this;
		}

		@Override
		public BuildStep name(String name) {

			this.expressionXformDefn.setNAME(name);

			return this;
		}

		@Override
		public SetInterPolationValues marshaller(Jaxb2Marshaller marshaller) {

			this.marshaller = marshaller;

			return this;
		}

		@Override
		public LoadFromSeedStep setInterpolationValues(Map<String, String> values) {
			this.interpolationValues = values;
			return this;
		}

		private static TRANSFORMFIELD expressionXformFieldFrom(InfaSourceColumnDefinition column) {

			TRANSFORMFIELD field = new TRANSFORMFIELD();
			field.setDATATYPE("String");
			field.setDEFAULTVALUE("");
			field.setDESCRIPTION("");
			field.setNAME(column.getColumnName());
			field.setPICTURETEXT("");
			field.setPORTTYPE("INPUT/OUTPUT");
			field.setPRECISION(Integer.toString(column.getPrecision()));
			field.setSCALE(Integer.toString(column.getScale()));

			return field;

		}

		@Override
		public AddFieldsStep expression(String expressionName) {
			expressionXformDefn = new TRANSFORMATION();

			expressionXformDefn.setNAME(expressionName);
			expressionXformDefn.setTYPE("Expression");
			expressionXformDefn.setREUSABLE("NO");
			expressionXformDefn.setDESCRIPTION("");
			expressionXformDefn.setOBJECTVERSION(DEFAULT_VERSION.getValue());
			expressionXformDefn.setVERSIONNUMBER(DEFAULT_VERSION.getValue());
			TABLEATTRIBUTE tracingAttribute = new TABLEATTRIBUTE();
			tracingAttribute.setNAME("Tracing Level");
			tracingAttribute.setVALUE("Normal");
			expressionXformDefn.getTABLEATTRIBUTE().add(tracingAttribute);

			return this;
		}

		@Override
		public ExpressionStep expressionFromPrototype(String className) {
			this.className = className;
			return this;
		}

		@Override
		public SetMarshallerStep ExpressionFromSeed(String className) {
			this.className = className;
			return this;
		}

		@Override
		public BuildStep nameAlreadySet() {

			return this;
		}

		@Override
		public AddFieldsStep addFields(List<InfaSourceColumnDefinition> columns) {

			this.expressionXformDefn.getTRANSFORMFIELD()
					.addAll(columns.stream()//
							.map(column -> expressionXformFieldFrom(column))//
							.collect(Collectors.toList()));

			return this;
		}

		@Override
		public AddFieldsStep addRowWidField() {

			TRANSFORMFIELD xformExpressionField = new TRANSFORMFIELD();
			xformExpressionField.setDATATYPE("decimal");
			xformExpressionField.setDEFAULTVALUE("");
			xformExpressionField.setDESCRIPTION("");
			xformExpressionField.setEXPRESSION("ROW_WID");
			xformExpressionField.setEXPRESSIONTYPE("GENERAL");
			xformExpressionField.setNAME("ROW_WID");
			xformExpressionField.setPICTURETEXT("");
			xformExpressionField.setPORTTYPE("INPUT/OUTPUT");
			xformExpressionField.setPRECISION("10");
			xformExpressionField.setSCALE("0");

			this.expressionXformDefn.getTRANSFORMFIELD().add(xformExpressionField);

			return this;
		}
		
		
		@Override
		public AddFieldsStep addDatasourceNumIdField() {

			TRANSFORMFIELD xformExpressionField = new TRANSFORMFIELD();
			xformExpressionField.setDATATYPE("decimal");
			xformExpressionField.setDEFAULTVALUE("");
			xformExpressionField.setDESCRIPTION("");
			xformExpressionField.setEXPRESSION("$$DATASOURCE_NUM_ID");
			xformExpressionField.setEXPRESSIONTYPE("GENERAL");
			xformExpressionField.setNAME("DATASOURCE_NUM_ID");
			xformExpressionField.setPICTURETEXT("");
			xformExpressionField.setPORTTYPE("OUTPUT");
			xformExpressionField.setPRECISION("10");
			xformExpressionField.setSCALE("0");

			this.expressionXformDefn.getTRANSFORMFIELD().add(xformExpressionField);

			return this;
		}
		
		
		
		

		@Override
		public AddFieldsStep addEtlProcWidField() {

			TRANSFORMFIELD xformExpressionField = new TRANSFORMFIELD();
			xformExpressionField.setDATATYPE("decimal");
			xformExpressionField.setDEFAULTVALUE("");
			xformExpressionField.setDESCRIPTION("");
			xformExpressionField.setEXPRESSION("$$ETL_PROC_WID");
			xformExpressionField.setEXPRESSIONTYPE("GENERAL");
			xformExpressionField.setNAME("ETL_PROC_WID");
			xformExpressionField.setPICTURETEXT("");
			xformExpressionField.setPORTTYPE("OUTPUT");
			xformExpressionField.setPRECISION("10");
			xformExpressionField.setSCALE("0");

			this.expressionXformDefn.getTRANSFORMFIELD().add(xformExpressionField);
			return this;
		}

		@Override
		public AddFieldsStep addEffectiveFromDateField() {

			TRANSFORMFIELD xformExpressionField = new TRANSFORMFIELD();
			xformExpressionField.setDATATYPE("date/time");
			xformExpressionField.setDEFAULTVALUE("");
			xformExpressionField.setDESCRIPTION("Every changed record will have a unique effective from timestamp");
			xformExpressionField.setEXPRESSION("SESSSTARTTIME");
			xformExpressionField.setEXPRESSIONTYPE("GENERAL");
			xformExpressionField.setNAME("EFF_FROM_DT");
			xformExpressionField.setPICTURETEXT("");
			xformExpressionField.setPORTTYPE("OUTPUT");
			xformExpressionField.setPRECISION("29");
			xformExpressionField.setSCALE("9");

			this.expressionXformDefn.getTRANSFORMFIELD().add(xformExpressionField);

			return this;
		}

		@Override
		public AddFieldsStep addIntegrationIdField(List<InfaSourceColumnDefinition> columns) {

			String concatenatedId = columns.stream()//
					.filter(InfaSourceColumnDefinition::getIntegrationIdFlag)//
					.map(ExpressionXformSteps::getInfaCastToStringExpression)//
					.collect(Collectors.joining("|| ':' ||"));

			TRANSFORMFIELD xformExpressionField = new TRANSFORMFIELD();
			xformExpressionField.setDATATYPE("string");
			xformExpressionField.setDEFAULTVALUE("");
			xformExpressionField.setDESCRIPTION("");
			xformExpressionField.setEXPRESSION(concatenatedId);
			xformExpressionField.setEXPRESSIONTYPE("GENERAL");
			xformExpressionField.setNAME("INTEGRATION_ID");
			xformExpressionField.setPICTURETEXT("");
			xformExpressionField.setPORTTYPE("OUTPUT");
			xformExpressionField.setPRECISION("100");
			xformExpressionField.setSCALE("0");

			this.expressionXformDefn.getTRANSFORMFIELD().add(xformExpressionField);

			return this;
		}

		@Override
		public AddFieldsStep addPGUIDField(String sourceName, List<InfaSourceColumnDefinition> columns) {

			String concatenatedId = columns.stream()//
					.filter(InfaSourceColumnDefinition::getIntegrationIdFlag)//
					.map(ExpressionXformSteps::getInfaCastToStringExpression)//
					.collect(Collectors.joining("|| ':' ||"));

			TRANSFORMFIELD xformExpressionField = new TRANSFORMFIELD();
			xformExpressionField.setDATATYPE("string");
			xformExpressionField.setDEFAULTVALUE("");
			xformExpressionField.setDESCRIPTION("");
			xformExpressionField.setEXPRESSION("'" + sourceName + "'||':' ||" + concatenatedId);
			xformExpressionField.setEXPRESSIONTYPE("GENERAL");
			xformExpressionField.setNAME("PGUID");
			xformExpressionField.setPICTURETEXT("");
			xformExpressionField.setPORTTYPE("OUTPUT");
			xformExpressionField.setPRECISION("100");
			xformExpressionField.setSCALE("0");

			this.expressionXformDefn.getTRANSFORMFIELD().add(xformExpressionField);

			return this;
		}

		@Override
		public AddFieldsStep addMD5HashField(List<InfaSourceColumnDefinition> columns) {

			String MD5Value = columns.stream()//
					.map(ExpressionXformSteps::getInfaCastToStringExpression)//
					.collect(Collectors.joining("||"));

			TRANSFORMFIELD xformExpressionField = new TRANSFORMFIELD();
			xformExpressionField.setDATATYPE("string");
			xformExpressionField.setDEFAULTVALUE("");
			xformExpressionField.setDESCRIPTION("");
			xformExpressionField.setEXPRESSION("MD5(" + MD5Value + ")");
			xformExpressionField.setEXPRESSIONTYPE("GENERAL");
			xformExpressionField.setNAME("HASH_RECORD");
			xformExpressionField.setPICTURETEXT("");
			xformExpressionField.setPORTTYPE("OUTPUT");
			xformExpressionField.setPRECISION("32");
			xformExpressionField.setSCALE("0");

			this.expressionXformDefn.getTRANSFORMFIELD().add(xformExpressionField);

			return this;
		}

		@Override
		public NameStep noMoreFields() {

			return this;
		}

		private static String getInfaCastToStringExpression(InfaSourceColumnDefinition coldef) {

			String colExpr = "";

			switch (coldef.getColumnDataType()) {
			case "date/time":
				colExpr = "TO_CHAR(" + coldef.getColumnName() + ",'YYYY-MM-DD HH24:MI:SS')";
				break;
			case "decimal":
				colExpr = "TO_CHAR(TO_INTEGER(" + coldef.getColumnName() + "))";
				break;
			default:
				colExpr = coldef.getColumnName();
				break;
			}

			return colExpr;

		}

	}

}
