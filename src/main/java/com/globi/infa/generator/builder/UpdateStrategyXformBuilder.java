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

import com.globi.infa.metadata.src.InfaSourceColumnDefinition;

import xjc.TRANSFORMATION;
import xjc.TRANSFORMFIELD;

public class UpdateStrategyXformBuilder {

	public static SetMarshallerStep newBuilder() {
		return new UpdateStrategyXformSteps();
	}


	public interface SetMarshallerStep {
		SetInterPolationValues marshaller(Jaxb2Marshaller marshaller);
	}

	public interface SetInterPolationValues {
		LoadFromSeedStep setInterpolationValues(Map<String, String> values);
		LoadFromSeedStep noInterpolationValues();
	}
	
	

	public interface LoadFromSeedStep {
		NameStep loadUpdateStrategyXformFromSeed(String seedName) throws FileNotFoundException, IOException;
	}

	public interface AddFieldsStep {
		
	
		AddFieldsStep addFields(List<InfaSourceColumnDefinition> columns);
		NameStep noMoreFields();
		
	}

	public interface NameStep {
		BuildStep name(String name);
		BuildStep nameAlreadySet();
	}

	public interface BuildStep {
		TRANSFORMATION build();
	}

	public static class UpdateStrategyXformSteps implements  NameStep, SetMarshallerStep,
			SetInterPolationValues, LoadFromSeedStep, AddFieldsStep,BuildStep {

		private Jaxb2Marshaller marshaller;
		private TRANSFORMATION updateStrategyXformDefn;
		private Map<String, String> interpolationValues;

		@Override
		public TRANSFORMATION build() {

			return this.updateStrategyXformDefn;
		}



		@Override
		public NameStep loadUpdateStrategyXformFromSeed(String seedName) throws FileNotFoundException, IOException {

			InputStream is = null;

			try {
				Resource resource = new ClassPathResource("seed/" + seedName + ".xml");
				is = resource.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				FileCopyUtils.copy(is, baos);
				StrSubstitutor sub = new StrSubstitutor(interpolationValues, "{{", "}}");

				this.updateStrategyXformDefn = (TRANSFORMATION) marshaller
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

			this.updateStrategyXformDefn.setNAME(name);

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
		public LoadFromSeedStep noInterpolationValues() {
			return this;
		}

		
		
		private TRANSFORMFIELD fieldFrom(InfaSourceColumnDefinition column){
			TRANSFORMFIELD updField = new TRANSFORMFIELD();
			updField.setDATATYPE("string");
			updField.setDEFAULTVALUE("");
			updField.setDESCRIPTION("");
			updField.setNAME("SYS_PGUID");
			updField.setPICTURETEXT("");
			updField.setPORTTYPE("OUTPUT");
			updField.setPRECISION("100");
			updField.setSCALE("0");
			return updField;

		}



		@Override
		public AddFieldsStep addFields(List<InfaSourceColumnDefinition> columns) {
			this.updateStrategyXformDefn.getTRANSFORMFIELD()
			.addAll(columns.stream()//
					.map(column -> fieldFrom(column))//
					.collect(Collectors.toList()));
			
			return this;
		}



		@Override
		public NameStep noMoreFields() {
			return this;
		}
			
		}
		

		

	}


