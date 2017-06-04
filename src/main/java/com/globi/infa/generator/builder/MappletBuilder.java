package com.globi.infa.generator.builder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.transform.stream.StreamSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import xjc.MAPPLET;

public class MappletBuilder {
	

	public static SetMarshallerStep newBuilder() {
		return new MappletSteps();
	}
	
	public interface SetMarshallerStep {
		LoadFromSeedStep marshaller(Jaxb2Marshaller marshaller);
	}
	
	public interface LoadFromSeedStep {
		NameStep loadMappletFromSeed(String seedName) throws FileNotFoundException, IOException;
	}

	
	public interface NameStep {
		BuildStep name(String name);
		BuildStep nameAlreadySet();
	}

	
	public interface BuildStep {
		MAPPLET build();
	}
	
	
	public static class MappletSteps implements NameStep,SetMarshallerStep,LoadFromSeedStep,BuildStep{

		
	 	private Jaxb2Marshaller marshaller;
	 	private MAPPLET mapplet;
		
		
		@Override
		public MAPPLET build() {

			return this.mapplet;
		}

		

		@Override
		public NameStep loadMappletFromSeed(String seedName) throws FileNotFoundException, IOException {
			
			
			
			FileInputStream is = null;

			try {
				Resource resource = new ClassPathResource("seed/" + seedName + ".xml");
				is = new FileInputStream(resource.getFile());
				this.mapplet = (MAPPLET) marshaller.unmarshal(new StreamSource(is));
			} finally {
				if (is != null) {
					is.close();
				}
			}
			
			return this;
		}


		@Override
		public BuildStep name(String name) {
			
			this.mapplet.setNAME(name);
			
			return this;
		}

		@Override
		public LoadFromSeedStep marshaller(Jaxb2Marshaller marshaller) {
			
			this.marshaller=marshaller;
			
			return this;
		}



		@Override
		public BuildStep nameAlreadySet() {
			return this;
		}
		
		


		
		
	}
	
	
	
	
}
