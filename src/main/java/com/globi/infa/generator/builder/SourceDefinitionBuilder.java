package com.globi.infa.generator.builder;

import static com.globi.infa.generator.InfaGeneratorDefaults.DEFAULT_DESCRIPTION;
import static com.globi.infa.generator.InfaGeneratorDefaults.DEFAULT_VERSION;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.metadata.src.InfaSourceDefinition;

import xjc.SOURCE;
import xjc.SOURCEFIELD;

public class SourceDefinitionBuilder {
	

	public static ClassStep newBuilder() {
		return new SourceDefinitionSteps();
	}
	
	
	public interface ClassStep {
	SourceDefnStep sourceDefnFromPrototype(String className);
	SetMarshallerStep sourceFromSeed(String className);
	} 

	public interface SourceDefnStep {
		AddFieldsStep sourceDefn(InfaSourceDefinition source);
	}

	
	
	public interface SetMarshallerStep {
		LoadFromSeedStep marshaller(Jaxb2Marshaller marshaller);
	}
	
	public interface LoadFromSeedStep {
		AddFieldsStep loadSourceFromSeed(String seedName) throws FileNotFoundException, IOException;
	}


	public interface AddFieldsStep {
		NameStep addFields(List<InfaSourceColumnDefinition> columns);
	}

	
	public interface NameStep {
		BuildStep name(String name);
	}

	
	public interface BuildStep {
		SOURCE build();
	}
	
	
	public static class SourceDefinitionSteps implements SourceDefnStep, ClassStep,NameStep,SetMarshallerStep,LoadFromSeedStep,AddFieldsStep,BuildStep{

		
	 	private Jaxb2Marshaller marshaller;
	 	private SOURCE sourceDefinition;
	 	@SuppressWarnings("unused")
		private String className;
		
		
		@Override
		public SOURCE build() {

			return this.sourceDefinition;
		}

		@Override
		public NameStep addFields(List<InfaSourceColumnDefinition> columns) {

		
			columns.forEach(column -> {
				this.sourceDefinition.getSOURCEFIELD().add(sourceFieldFrom(column));
			});

			
			return this;
		}
		
		

		@Override
		public AddFieldsStep loadSourceFromSeed(String seedName) throws FileNotFoundException, IOException {
			
			
			
			FileInputStream is = null;

			try {
				Resource resource = new ClassPathResource("seed/" + seedName + ".xml");
				is = new FileInputStream(resource.getFile());
				this.sourceDefinition = (SOURCE) marshaller.unmarshal(new StreamSource(is));
			} finally {
				if (is != null) {
					is.close();
				}
			}
			
			return this;
		}


		@Override
		public BuildStep name(String name) {
			
			this.sourceDefinition.setNAME(name);
			
			return this;
		}

		@Override
		public LoadFromSeedStep marshaller(Jaxb2Marshaller marshaller) {
			
			this.marshaller=marshaller;
			
			return this;
		}
		
		
		private static SOURCEFIELD sourceFieldFrom( InfaSourceColumnDefinition column) {

			SOURCEFIELD sourceField = new SOURCEFIELD();

			sourceField.setBUSINESSNAME(DEFAULT_DESCRIPTION.getValue());
			sourceField.setDATATYPE(column.getColumnDataType());
			sourceField.setFIELDNUMBER(Integer.toString(column.getColumnNumber()));
			sourceField.setNULLABLE(column.getNullable());
			sourceField.setNAME(column.getColumnName());
			sourceField.setOCCURS("0");
			sourceField.setHIDDEN("NO");
			sourceField.setFIELDPROPERTY("0");
			sourceField.setFIELDTYPE("ELEMITEM");
			sourceField.setKEYTYPE("NOT A KEY");
			sourceField.setLENGTH(Integer.toString(column.getColumnLength()));
			sourceField.setLEVEL("0");
			sourceField.setOFFSET(Integer.toString(column.getOffset()));
			sourceField.setPHYSICALLENGTH(Integer.toString(column.getPhysicalLength()));
			sourceField.setPHYSICALOFFSET(Integer.toString(column.getPhysicalOffset()));
			sourceField.setPICTURETEXT("");
			sourceField.setPRECISION(Integer.toString(column.getPrecision()));
			sourceField.setSCALE(Integer.toString(column.getScale()));
			sourceField.setUSAGEFLAGS("");


			return sourceField;

		}


		@Override
		public SetMarshallerStep sourceFromSeed(String className) {
		
			return this;
		}

		@Override
		public SourceDefnStep sourceDefnFromPrototype(String className) {
			
			this.className=className;
			
			return this;
		}

		@Override
		public AddFieldsStep sourceDefn(InfaSourceDefinition source) {
			
			
			SOURCE sourceDefn = new SOURCE();

			sourceDefn.setBUSINESSNAME("");
			sourceDefn.setDATABASETYPE(source.getDatabaseType());
			sourceDefn.setDBDNAME(source.getDatabaseName());
			sourceDefn.setDESCRIPTION(DEFAULT_DESCRIPTION.getValue());
			sourceDefn.setNAME(source.getSourceTableName());
			sourceDefn.setOBJECTVERSION(DEFAULT_VERSION.getValue());
			sourceDefn.setOWNERNAME(source.getOwnerName());
			sourceDefn.setVERSIONNUMBER(DEFAULT_VERSION.getValue());
			
			this.sourceDefinition=sourceDefn;
			
			
			
			return this;
		}
		
		
		
	}
	
	
	
	
}
