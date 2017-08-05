package com.globi.infa.generator.builder;

import static com.globi.infa.generator.InfaGeneratorDefaults.DEFAULT_VERSION;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

import com.globi.infa.datasource.core.DataTypeMapper;
import com.globi.infa.metadata.core.StringMap;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.metadata.src.PTPInfaSourceColumnDefinition;

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
		SetMapperStep expression(String expressionName);
	}

	public interface SetMarshallerStep {
		SetInterPolationValues marshaller(Jaxb2Marshaller marshaller);
	}

	public interface SetInterPolationValues {
		LoadFromSeedStep setInterpolationValues(Map<String, String> values);

	}

	public interface LoadFromSeedStep {
		SetMapperStep loadExpressionXformFromSeed(String seedName) throws FileNotFoundException, IOException;
	}

	public interface SetMapperStep {
		AddFieldsStep mapper(DataTypeMapper mapper);

	}

	public interface AddFieldsStep {
		
		AddFieldsStep addInputField(String name, String dataType,String precision,String scale);
		
		AddFieldsStep addFields(List<InfaSourceColumnDefinition> columns);

		AddFieldsStep addBUIDField(List<PTPInfaSourceColumnDefinition> columns);

		AddFieldsStep addRowWidField();

		AddFieldsStep addEtlProcWidField(String name);

		AddFieldsStep addEffectiveFromDateField();

		AddFieldsStep addIntegrationIdField(List<PTPInfaSourceColumnDefinition> columns);

		AddFieldsStep addDatasourceNumIdField();

		AddFieldsStep addPGUIDField(String sourceName, String tableName, StringMap sourceTableAbbr,
				List<PTPInfaSourceColumnDefinition> columns);

		AddFieldsStep addMD5HashField(String name,List<InfaSourceColumnDefinition> columns);

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
			SetInterPolationValues, LoadFromSeedStep, AddFieldsStep, BuildStep, SetMapperStep {

		private Jaxb2Marshaller marshaller;
		private TRANSFORMATION expressionXformDefn;
		private Map<String, String> interpolationValues;
		private DataTypeMapper mapper;

		@SuppressWarnings("unused")
		private String className;

		@Override
		public TRANSFORMATION build() {

			return this.expressionXformDefn;
		}

		@Override
		public SetMapperStep loadExpressionXformFromSeed(String seedName) throws FileNotFoundException, IOException {

			InputStream is = null;

			try {
				Resource resource = new ClassPathResource("seed/" + seedName + ".xml");
				is = resource.getInputStream();
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
		public SetMapperStep expression(String expressionName) {
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
			xformExpressionField.setEXPRESSION("SYS_ROW_WID");
			xformExpressionField.setEXPRESSIONTYPE("GENERAL");
			xformExpressionField.setNAME("SYS_ROW_WID");
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
			xformExpressionField.setEXPRESSION("$$SYS_DATASOURCE_NUM_ID");
			xformExpressionField.setEXPRESSIONTYPE("GENERAL");
			xformExpressionField.setNAME("SYS_DATASOURCE_NUM_ID");
			xformExpressionField.setPICTURETEXT("");
			xformExpressionField.setPORTTYPE("OUTPUT");
			xformExpressionField.setPRECISION("10");
			xformExpressionField.setSCALE("0");

			this.expressionXformDefn.getTRANSFORMFIELD().add(xformExpressionField);

			return this;
		}

		@Override
		public AddFieldsStep addEtlProcWidField(String name) {

			TRANSFORMFIELD xformExpressionField = new TRANSFORMFIELD();
			xformExpressionField.setDATATYPE("decimal");
			xformExpressionField.setDEFAULTVALUE("");
			xformExpressionField.setDESCRIPTION("");
			xformExpressionField.setEXPRESSION("$$" + name);
			xformExpressionField.setEXPRESSIONTYPE("GENERAL");
			xformExpressionField.setNAME(name);
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
			xformExpressionField.setNAME("SYS_EFF_FROM_DT");
			xformExpressionField.setPICTURETEXT("");
			xformExpressionField.setPORTTYPE("OUTPUT");
			xformExpressionField.setPRECISION("29");
			xformExpressionField.setSCALE("9");

			this.expressionXformDefn.getTRANSFORMFIELD().add(xformExpressionField);

			return this;
		}

		@Override
		public AddFieldsStep addIntegrationIdField(List<PTPInfaSourceColumnDefinition> columns) {

			String concatenatedId = columns.stream()//
					.filter(PTPInfaSourceColumnDefinition::getIntegrationIdFlag)//
					.sorted((e1, e2) -> Integer.compare(e1.getColumnSequence(), e2.getColumnSequence()))
					.map(this::getInfaCastToStringExpression)//
					.collect(Collectors.joining("|| ':' ||"));

			if (concatenatedId.isEmpty()) {
				concatenatedId = "'INTEGRATION_ID not set'";
			}

			TRANSFORMFIELD xformExpressionField = new TRANSFORMFIELD();
			xformExpressionField.setDATATYPE("string");
			xformExpressionField.setDEFAULTVALUE("");
			xformExpressionField.setDESCRIPTION("");
			xformExpressionField.setEXPRESSION(concatenatedId);
			xformExpressionField.setEXPRESSIONTYPE("GENERAL");
			xformExpressionField.setNAME("SYS_INTEGRATION_ID");
			xformExpressionField.setPICTURETEXT("");
			xformExpressionField.setPORTTYPE("OUTPUT");
			xformExpressionField.setPRECISION("100");
			xformExpressionField.setSCALE("0");

			this.expressionXformDefn.getTRANSFORMFIELD().add(xformExpressionField);

			return this;
		}

		@Override
		public AddFieldsStep addBUIDField(List<PTPInfaSourceColumnDefinition> columns) {

			String concatenatedId = columns.stream()//
					.filter(PTPInfaSourceColumnDefinition::getBuidFlag)//
					.sorted((e1, e2) -> Integer.compare(e1.getColumnSequence(), e2.getColumnSequence()))
					.map(this::getInfaCastToStringExpression)//
					.collect(Collectors.joining("|| ':' ||"));

			if (concatenatedId.isEmpty()) {
				concatenatedId = "'BU not set'";
			}

			TRANSFORMFIELD xformExpressionField = new TRANSFORMFIELD();
			xformExpressionField.setDATATYPE("string");
			xformExpressionField.setDEFAULTVALUE("");
			xformExpressionField.setDESCRIPTION("");
			xformExpressionField.setEXPRESSION("IIF(ISNULL(:LKP.LKP_BU({{buid}})),{{buid}},:LKP.LKP_BU({{buid}}))"
					.replace("{{buid}}", concatenatedId));
			xformExpressionField.setEXPRESSIONTYPE("GENERAL");
			xformExpressionField.setNAME("BU");
			xformExpressionField.setPICTURETEXT("");
			xformExpressionField.setPORTTYPE("OUTPUT");
			xformExpressionField.setPRECISION("100");
			xformExpressionField.setSCALE("0");

			this.expressionXformDefn.getTRANSFORMFIELD().add(xformExpressionField);

			return this;
		}

		@Override
		public AddFieldsStep addPGUIDField(String sourceName, String tableName, StringMap sourceTableAbbr,
				List<PTPInfaSourceColumnDefinition> columns) {

			String concatenatedId = columns.stream()//
					.filter(PTPInfaSourceColumnDefinition::getPguidFlag)//
					.sorted((e1, e2) -> Integer.compare(e1.getColumnSequence(), e2.getColumnSequence()))
					.map(this::getInfaCastToStringExpression)//
					.collect(Collectors.joining("|| ':' ||"));

			String integrationId = columns.stream()//
					.filter(PTPInfaSourceColumnDefinition::getIntegrationIdFlag)//
					.sorted((e1, e2) -> Integer.compare(e1.getColumnSequence(), e2.getColumnSequence()))
					.map(this::getInfaCastToStringExpression)//
					.collect(Collectors.joining("|| ':' ||"));

			if (concatenatedId.isEmpty()) {
				concatenatedId = integrationId;
			}

			concatenatedId = "'" + sourceTableAbbr.map(sourceName + "_" + tableName) + "'" + " || " + concatenatedId;

			String expPguidColsNullsCheck = "ISNULL(" + columns.stream()//
					.filter(PTPInfaSourceColumnDefinition::getPguidFlag)//
					.sorted((e1, e2) -> Integer.compare(e1.getColumnSequence(), e2.getColumnSequence()))
					.map(col -> col.getColumnName())//
					.collect(Collectors.joining(") AND ISNULL(")) + ")";

			// If all PGUID columns have a NULL value, then set to Int Id
			String pguidWithIntIdIfNullExpression = "IIF(" + expPguidColsNullsCheck + "," + integrationId + ","
					+ concatenatedId + ")";

			TRANSFORMFIELD xformExpressionField = new TRANSFORMFIELD();
			xformExpressionField.setDATATYPE("string");
			xformExpressionField.setDEFAULTVALUE("");
			xformExpressionField.setDESCRIPTION("");
			xformExpressionField.setEXPRESSION(pguidWithIntIdIfNullExpression);
			xformExpressionField.setEXPRESSIONTYPE("GENERAL");
			xformExpressionField.setNAME("SYS_PGUID");
			xformExpressionField.setPICTURETEXT("");
			xformExpressionField.setPORTTYPE("OUTPUT");
			xformExpressionField.setPRECISION("100");
			xformExpressionField.setSCALE("0");

			this.expressionXformDefn.getTRANSFORMFIELD().add(xformExpressionField);

			return this;
		}

		@Override
		public AddFieldsStep addMD5HashField(String name,List<InfaSourceColumnDefinition> columns) {

			String MD5Value = columns.stream()//
					.map(this::getInfaCastToStringExpression)//
					.collect(Collectors.joining("||"));

			TRANSFORMFIELD xformExpressionField = new TRANSFORMFIELD();
			xformExpressionField.setDATATYPE("string");
			xformExpressionField.setDEFAULTVALUE("");
			xformExpressionField.setDESCRIPTION("");
			xformExpressionField.setEXPRESSION("MD5(" + MD5Value + ")");
			xformExpressionField.setEXPRESSIONTYPE("GENERAL");
			xformExpressionField.setNAME(name);
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

		private String getInfaCastToStringExpression(InfaSourceColumnDefinition coldef) {

			String colExpr = "";

			// map to Xform type before casting
			String dataType = mapper.mapType(coldef.getColumnDataType());
			String colName = coldef.getColumnName();

			if (dataType.equals("date/time"))
				colExpr = "TO_CHAR(" + colName + ",'YYYY-MM-DD HH24:MI:SS')";
			else if (dataType.equals("decimal") || dataType.equals("double"))
				colExpr = "TO_CHAR(TO_INTEGER(" + colName + "))";
			else if (dataType.equals("integer") || dataType.equals("bigint") || dataType.equals("small integer"))
				colExpr = "TO_CHAR(" + colName + ")";
			else if (dataType.equals("binary"))
				colExpr = "ENC_BASE64(" + colName + ")";
			else
				colExpr = colName;

			colExpr = "IIF(ISNULL(" + colExpr + "),'NOVAL'," + colExpr + ")";

			return colExpr;

		}

		@Override
		public AddFieldsStep mapper(DataTypeMapper mapper) {

			this.mapper = mapper;

			return this;
		}

		@Override
		public AddFieldsStep addInputField(String name, String dataType, String precision, String scale) {


			TRANSFORMFIELD xformExpressionField = new TRANSFORMFIELD();
			xformExpressionField.setDATATYPE(dataType);
			xformExpressionField.setDEFAULTVALUE("");
			xformExpressionField.setDESCRIPTION("");
			xformExpressionField.setNAME(name);
			xformExpressionField.setPICTURETEXT("");
			xformExpressionField.setPORTTYPE("INPUT");
			xformExpressionField.setPRECISION(precision);
			xformExpressionField.setSCALE(scale);

			this.expressionXformDefn.getTRANSFORMFIELD().add(xformExpressionField);
			
			return this;
		}

	}

}
