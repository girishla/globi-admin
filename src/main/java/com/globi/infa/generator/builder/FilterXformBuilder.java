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

import com.globi.infa.generator.builder.FilterXformBuilder.AddFieldsStep;
import com.globi.infa.metadata.source.InfaSourceColumnDefinition;

import xjc.TABLEATTRIBUTE;
import xjc.TRANSFORMATION;
import xjc.TRANSFORMFIELD;

public class FilterXformBuilder {

	public static ClassStep newBuilder() {
		return new FilterXformSteps();
	}

	public interface ClassStep {
		FilterStep filterFromPrototype(String className);
		SetMarshallerStep FilterFromSeed(String className);
	}

	public interface FilterStep {
		AddFieldsStep filter(String filterName);

	}

	public interface SetMarshallerStep {
		SetInterPolationValues marshaller(Jaxb2Marshaller marshaller);
	}

	public interface SetInterPolationValues {
		LoadFromSeedStep setInterpolationValues(Map<String, String> values);

	}

	public interface LoadFromSeedStep {
		AddFieldsStep loadFilterXformFromSeed(String seedName) throws FileNotFoundException, IOException;
	}

	public interface AddFieldsStep {
		AddFieldsStep addFields(List<InfaSourceColumnDefinition> columns);
		AddFieldsStep addPGUIDField();
		AddFilterCondition noMoreFields();
		
	}
	
	
	public interface AddFilterCondition {
		AddFilterCondition addCondition(String conditionExpression);
		NameStep noMoreConditions();
		
	}
	
	


	public interface NameStep {
		BuildStep name(String name);
		BuildStep nameAlreadySet();
	}

	public interface BuildStep {
		TRANSFORMATION build();
	}

	public static class FilterXformSteps implements ClassStep, FilterStep, NameStep, SetMarshallerStep,
			SetInterPolationValues, LoadFromSeedStep, AddFieldsStep,AddFilterCondition, BuildStep {

		private Jaxb2Marshaller marshaller;
		private TRANSFORMATION filterXformDefn;
		private Map<String, String> interpolationValues;
		@SuppressWarnings("unused")
		private String className;

		@Override
		public TRANSFORMATION build() {

			return this.filterXformDefn;
		}



		@Override
		public AddFieldsStep loadFilterXformFromSeed(String seedName) throws FileNotFoundException, IOException {

			FileInputStream is = null;

			try {
				Resource resource = new ClassPathResource("seed/" + seedName + ".xml");
				is = new FileInputStream(resource.getFile());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				FileCopyUtils.copy(is, baos);
				StrSubstitutor sub = new StrSubstitutor(interpolationValues, "{{", "}}");

				this.filterXformDefn = (TRANSFORMATION) marshaller
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

			this.filterXformDefn.setNAME(name);

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

		private static TRANSFORMFIELD filterXformFieldFrom(InfaSourceColumnDefinition column) {

			TRANSFORMFIELD field = new TRANSFORMFIELD();
			field.setDATATYPE("string");
			field.setDEFAULTVALUE("");
			field.setDESCRIPTION("");
			field.setNAME(column.getColumnName());
			field.setPICTURETEXT("");
			field.setPORTTYPE("INPUT/OUTPUT");
			field.setPRECISION(Integer.toString(column.getPrecision()));
			field.setSCALE(Integer.toString(column.getScale()));
			
			return field;

		}
		
		private static TRANSFORMFIELD filterXformFieldFrom(String expression) {

			TRANSFORMFIELD field = new TRANSFORMFIELD();
			field.setDATATYPE("string");
			field.setDEFAULTVALUE("");
			field.setDESCRIPTION("");
			field.setNAME(expression);
			field.setPICTURETEXT("");
			field.setPORTTYPE("INPUT/OUTPUT");
			field.setPRECISION("100");
			field.setSCALE("0");
			
			return field;

		}
		

		@Override
		public AddFieldsStep filter(String filterName) {
			filterXformDefn=new TRANSFORMATION();
			
			filterXformDefn.setNAME(filterName);
			filterXformDefn.setTYPE("Filter");
			filterXformDefn.setREUSABLE("NO");
			filterXformDefn.setDESCRIPTION("");
			filterXformDefn.setOBJECTVERSION(DEFAULT_VERSION.getValue());
			filterXformDefn.setVERSIONNUMBER(DEFAULT_VERSION.getValue());
			
			
			TABLEATTRIBUTE tracingAttribute=new TABLEATTRIBUTE();
			tracingAttribute.setNAME("Tracing Level");
			tracingAttribute.setVALUE("Normal");
			filterXformDefn.getTABLEATTRIBUTE().add(tracingAttribute);

			
			return this;
		}

		
		@Override
		public FilterStep filterFromPrototype(String className) {
			this.className = className;
			return this;
		}

		@Override
		public SetMarshallerStep FilterFromSeed(String className) {
			this.className = className;
			return this;
		}

		@Override
		public BuildStep nameAlreadySet() {
		
			return this;
		}

		@Override
		public AddFieldsStep addFields(List<InfaSourceColumnDefinition> columns) {
			
			this.filterXformDefn.getTRANSFORMFIELD()
			.addAll(columns.stream()//
					.map(column -> filterXformFieldFrom(column))//
					.collect(Collectors.toList()));
			
			return this;
		}
		
		
		@Override
		public AddFieldsStep addPGUIDField() {
			
			this.filterXformDefn.getTRANSFORMFIELD()
			.add(filterXformFieldFrom("BU_PGUID"));
			
			return this;
		}
		


		@Override
		public AddFilterCondition noMoreFields() {
			
			
			return this;
		}



		@Override
		public AddFilterCondition addCondition(String conditionExpression) {

			TABLEATTRIBUTE filterCondition = new TABLEATTRIBUTE();
			filterCondition.setNAME("Filter Condition");
			filterCondition.setVALUE(conditionExpression);
			filterXformDefn.getTABLEATTRIBUTE().add(filterCondition);
			
			return this;
		}


		

		@Override
		public NameStep noMoreConditions() {

			return this;
		}


	}

}
