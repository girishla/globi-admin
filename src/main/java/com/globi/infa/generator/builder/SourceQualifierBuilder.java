package com.globi.infa.generator.builder;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.util.FileCopyUtils;

import com.globi.infa.datasource.core.DataTypeMapper;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.workflow.PTPWorkflowSourceColumn;

import xjc.TABLEATTRIBUTE;
import xjc.TRANSFORMATION;
import xjc.TRANSFORMFIELD;

public class SourceQualifierBuilder {

	public static SetMarshallerStep newBuilder() {
		return new SourceQualifierSteps();
	}

	public interface SetMarshallerStep {
		SetInterpolationValueStep marshaller(Jaxb2Marshaller marshaller);
	}

	public interface SetInterpolationValueStep {
		SetInterpolationValueStep setValue(String name, String value);

		LoadFromSeedStep noMoreValues();
	}

	public interface LoadFromSeedStep {
		AddFieldsStep loadSourceQualifierFromSeed(String seedName) throws FileNotFoundException, IOException;
	}

	public interface AddFieldsStep {
		AddFilterStep addFields(DataTypeMapper mapper, List<InfaSourceColumnDefinition> columns);
		AddFilterStep noMoreFields();
	}

	public interface AddFilterStep {
		AddFilterStep addFilter(String filter);

		AddFilterStep addCCFilterFromColumns(List<PTPWorkflowSourceColumn> inputSelectedColumns, String tableName);

		NameStep noMoreFilters();
	}

	public interface NameStep {
		BuildStep name(String name);
		BuildStep nameAlreadySet();
	}

	public interface BuildStep {
		TRANSFORMATION build();
	}

	public static class SourceQualifierSteps implements NameStep, SetMarshallerStep, SetInterpolationValueStep,
			LoadFromSeedStep, AddFieldsStep, AddFilterStep, BuildStep {

		private Jaxb2Marshaller marshaller;
		private TRANSFORMATION sourceQualifierDefn;
		private String filterString = "";
		private Map<String, String> interpolationValues = new HashMap<>();

		@Override
		public TRANSFORMATION build() {

			return this.sourceQualifierDefn;
		}

		@Override
		public AddFilterStep addFields(DataTypeMapper mapper, List<InfaSourceColumnDefinition> columns) {

			this.sourceQualifierDefn.getTRANSFORMFIELD()
					.addAll(columns.stream()//
							.map(column -> sourceQualifierFieldFrom(mapper, column))//
							.collect(Collectors.toList()));

			return this;
		}

		@Override
		public AddFieldsStep loadSourceQualifierFromSeed(String seedName) throws FileNotFoundException, IOException {

			InputStream is = null;

			try {
				Resource resource = new ClassPathResource("seed/" + seedName + ".xml");
				is = resource.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				FileCopyUtils.copy(is, baos);
				StrSubstitutor sub = new StrSubstitutor(interpolationValues, "{{", "}}");

				this.sourceQualifierDefn = (TRANSFORMATION) marshaller
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

			this.sourceQualifierDefn.setNAME(name);

			return this;
		}

		@Override
		public SetInterpolationValueStep marshaller(Jaxb2Marshaller marshaller) {

			this.marshaller = marshaller;

			return this;
		}

		private String getCCFilterString(List<PTPWorkflowSourceColumn> inputSelectedColumns, String tableName) {

			// Find and set the sourceQualifier filter column
			Optional<PTPWorkflowSourceColumn> sourceQualifierFilterClauseColumn = inputSelectedColumns.stream()//
					.filter(column -> column.isChangeCaptureColumn())//
					.findAny();

			String ccFilter = "";

			if (sourceQualifierFilterClauseColumn.isPresent()) {
				ccFilter = tableName + "." + sourceQualifierFilterClauseColumn.get().getSourceColumnName()
						+ " >= TO_DATE('$$INITIAL_EXTRACT_DATE','dd/MM/yyyy HH24:mi:ss')";
			}

			return ccFilter;
		}

		private static TRANSFORMFIELD sourceQualifierFieldFrom(DataTypeMapper mapper,
				InfaSourceColumnDefinition column) {

			String precision = column.getColumnDataType().equals("datetime") ? "19"
					: Integer.toString(column.getPrecision());
			String scale = column.getColumnDataType().equals("datetime") ? "0" : Integer.toString(column.getScale());

			TRANSFORMFIELD field = new TRANSFORMFIELD();
			field.setDATATYPE(mapper.mapType(column.getColumnDataType()));
			field.setDEFAULTVALUE("");
			field.setDESCRIPTION("");
			field.setNAME(column.getColumnName());
			field.setPICTURETEXT("");
			field.setPORTTYPE("INPUT/OUTPUT");
			field.setPRECISION(precision);
			field.setSCALE(scale);

			return field;

		}

		@Override
		public SetInterpolationValueStep setValue(String name, String value) {
			this.interpolationValues.put(name, value);
			return this;
		}

		@Override
		public LoadFromSeedStep noMoreValues() {
			return this;
		}

		@Override
		public AddFilterStep addFilter(String filter) {

			if (filter != null) {
				if (!filterString.isEmpty())
					filterString = filterString + " AND ";

				filterString += filter;
			}

			return this;
		}

		@Override
		public NameStep noMoreFilters() {

			TABLEATTRIBUTE filterAttribute = new TABLEATTRIBUTE();
			filterAttribute.setNAME("Source Filter");
			filterAttribute.setVALUE(filterString);
			sourceQualifierDefn.getTABLEATTRIBUTE().add(filterAttribute);

			return this;
		}

		@Override
		public AddFilterStep addCCFilterFromColumns(List<PTPWorkflowSourceColumn> inputSelectedColumns,
				String tableName) {

			if (!filterString.isEmpty())
				filterString = filterString + " AND ";

			filterString += this.getCCFilterString(inputSelectedColumns, tableName);
			return this;
		}

		@Override
		public AddFilterStep noMoreFields() {

			return this;
		}

		@Override
		public BuildStep nameAlreadySet() {

			return this;
		}

	}

}
