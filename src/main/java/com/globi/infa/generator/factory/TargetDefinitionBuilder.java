package com.globi.infa.generator.factory;

import static com.globi.infa.generator.InfagenDefaults.DEFAULT_DESCRIPTION;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.globi.infa.sourcedefinition.InfaSourceColumnDefinition;



import xjc.TARGET;
import xjc.TARGETFIELD;

public class TargetDefinitionBuilder {
	

	public static SetMarshallerStep newBuilder() {
		return new TargetDefinitionSteps();
	}
	
	public interface SetMarshallerStep {
		LoadFromSeedStep marshaller(Jaxb2Marshaller marshaller);
	}
	
	public interface LoadFromSeedStep {
		AddFieldsStep loadTargetFromSeed(String seedName) throws FileNotFoundException, IOException;
	}


	public interface AddFieldsStep {
		NameStep addFields(List<InfaSourceColumnDefinition> columns);
	}

	
	public interface NameStep {
		BuildStep name(String name);
	}

	
	public interface BuildStep {
		TARGET build();
	}
	
	
	public static class TargetDefinitionSteps implements NameStep,SetMarshallerStep,LoadFromSeedStep,AddFieldsStep,BuildStep{

		
	 	private Jaxb2Marshaller marshaller;
	 	private TARGET targetDefinition;
		
		
		@Override
		public TARGET build() {

			return this.targetDefinition;
		}

		@Override
		public NameStep addFields(List<InfaSourceColumnDefinition> columns) {

		
			columns.forEach(column -> {
				this.targetDefinition.getTARGETFIELD().add(targetFieldFrom(column));
			});

			
			return this;
		}
		
		

		@Override
		public AddFieldsStep loadTargetFromSeed(String seedName) throws FileNotFoundException, IOException {
			
			
			
			FileInputStream is = null;

			try {
				Resource resource = new ClassPathResource("seed/" + seedName + ".xml");
				is = new FileInputStream(resource.getFile());
				this.targetDefinition = (TARGET) marshaller.unmarshal(new StreamSource(is));
			} finally {
				if (is != null) {
					is.close();
				}
			}
			
			return this;
		}


		@Override
		public BuildStep name(String name) {
			
			this.targetDefinition.setNAME(name);
			
			return this;
		}

		@Override
		public LoadFromSeedStep marshaller(Jaxb2Marshaller marshaller) {
			
			this.marshaller=marshaller;
			
			return this;
		}
		
		
		private static TARGETFIELD targetFieldFrom(InfaSourceColumnDefinition column) {

			TARGETFIELD targetField = new TARGETFIELD();

			targetField.setBUSINESSNAME(DEFAULT_DESCRIPTION.getValue());
			targetField.setDATATYPE(column.getColumnDataType());
			targetField.setFIELDNUMBER(Integer.toString(column.getColumnNumber()));
			targetField.setNULLABLE(column.getNullable());
			targetField.setNAME(column.getColumnName());
			targetField.setKEYTYPE("NOT A KEY");
			targetField.setPICTURETEXT("");
			targetField.setPRECISION(Integer.toString(column.getPrecision()));
			targetField.setSCALE(Integer.toString(column.getScale()));

			return targetField;

		}
		
		
		
	}
	
	
	
	
}
