package com.globi.infa.generator.builder;

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
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.rits.cloning.Cloner;

import xjc.TRANSFORMATION;
import xjc.TRANSFORMFIELD;

public class UnionXformBuilder {

	public static SetMarshallerStep newBuilder() {
		return new UnionXformSteps();
	}

	public interface SetMarshallerStep {
		SetInterPolationValues marshaller(Jaxb2Marshaller marshaller);
	}

	public interface SetInterPolationValues {
		LoadFromSeedStep setInterpolationValues(Map<String, String> values);

		LoadFromSeedStep noMoreValues();

	}

	public interface LoadFromSeedStep {
		AddOutputFieldsStep loadUnionXformFromSeed(String seedName) throws FileNotFoundException, IOException;
	}

	public interface AddOutputFieldsStep {
		AddInputFieldsStep addOutputFields(DataTypeMapper mapper, List<InfaSourceColumnDefinition> columns);
	}

	public interface AddInputFieldsStep {
		AddInputFieldsStep addInputFields(String groupName, int fieldSuffix);
		NameStep noMoreInputFields();
	}

	public interface NameStep {
		BuildStep name(String name);

		BuildStep nameAlreadySet();
	}

	public interface BuildStep {
		TRANSFORMATION build();
	}

	public static class UnionXformSteps implements NameStep, SetMarshallerStep, SetInterPolationValues,
			LoadFromSeedStep, AddOutputFieldsStep, AddInputFieldsStep, BuildStep {

		private Jaxb2Marshaller marshaller;
		private TRANSFORMATION unionXformDefn;
		private Map<String, String> interpolationValues;

		@Override
		public TRANSFORMATION build() {

			return this.unionXformDefn;
		}

		@Override
		public AddOutputFieldsStep loadUnionXformFromSeed(String seedName) throws FileNotFoundException, IOException {

			InputStream is = null;

			try {
				Resource resource = new ClassPathResource("seed/" + seedName + ".xml");
				is = resource.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				FileCopyUtils.copy(is, baos);
				StrSubstitutor sub = new StrSubstitutor(interpolationValues, "{{", "}}");

				this.unionXformDefn = (TRANSFORMATION) marshaller
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

			this.unionXformDefn.setNAME(name);

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

		@Override
		public BuildStep nameAlreadySet() {

			return this;
		}

		@Override
		public LoadFromSeedStep noMoreValues() {
			return this;
		}

		private static TRANSFORMFIELD unionXformOutputFieldFrom(DataTypeMapper mapper,
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
			field.setPORTTYPE("OUTPUT");
			field.setGROUP("OUTPUT");
			field.setOUTPUTGROUP("OUTPUT");
			field.setPRECISION(precision);
			field.setSCALE(scale);

			return field;

		}

		@Override
		public AddInputFieldsStep addOutputFields(DataTypeMapper mapper, List<InfaSourceColumnDefinition> columns) {

			this.unionXformDefn.getTRANSFORMFIELD()
					.addAll(columns.stream()//
							.map(column -> unionXformOutputFieldFrom(mapper, column))//
							.collect(Collectors.toList()));

			return this;
		}

		@Override
		
		//Copies fields from Output ports
		public AddInputFieldsStep addInputFields(String groupName, int fieldSuffix) {

			Cloner cloner = Cloner.shared();
			
			List<TRANSFORMFIELD> inpFields = this.unionXformDefn.getTRANSFORMFIELD().stream()//
					.filter(field->field.getPORTTYPE().equals("OUTPUT"))
					.map(field -> {
						TRANSFORMFIELD newField = cloner.deepClone(field);
						newField.setGROUP(groupName);
						newField.setOUTPUTGROUP(groupName);
						newField.setPORTTYPE("INPUT");
						newField.setNAME(field.getNAME() + fieldSuffix);
						return newField;
					})//
					.collect(Collectors.toList());

			this.unionXformDefn.getTRANSFORMFIELD().addAll(inpFields);

			return this;
		}
		
		@Override
		public NameStep noMoreInputFields() {

			return this;
		}

	}

}
