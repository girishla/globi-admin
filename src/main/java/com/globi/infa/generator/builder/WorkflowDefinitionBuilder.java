package com.globi.infa.generator.builder;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.util.FileCopyUtils;

import xjc.WORKFLOW;

public class WorkflowDefinitionBuilder {
	

	public static SetMarshallerStep newBuilder() {
		return new WorkflowDefinitionSteps();
	}
	
	
	public interface SetMarshallerStep {
		SetInterpolationValue marshaller(Jaxb2Marshaller marshaller);
	}
	
	
	public interface SetInterpolationValue{
		SetInterpolationValue setValue(String name,String value);
		LoadFromSeedStep noMoreValues();
	}
	
	
	
	public interface LoadFromSeedStep {
		NameStep loadWorkflowFromSeed(String seedName) throws FileNotFoundException, IOException;
	}


	
	public interface NameStep {
		BuildStep name(String name);
		BuildStep nameAlreadySet();
	}

	
	public interface BuildStep {
		WORKFLOW build();
	}
	
	
	public static class WorkflowDefinitionSteps implements NameStep,SetMarshallerStep,SetInterpolationValue,LoadFromSeedStep,BuildStep{

		
	 	private Jaxb2Marshaller marshaller;
	 	private WORKFLOW workflowDefinition;
	 	private Map<String,String> interpolationValues=new HashMap<>();
	 	
		
		
		@Override
		public WORKFLOW build() {
			return this.workflowDefinition;
		}

	
		

		@Override
		public NameStep loadWorkflowFromSeed(String seedName) throws FileNotFoundException, IOException {
			
			FileInputStream is = null;

			try {
				Resource resource = new ClassPathResource("seed/" + seedName + ".xml");
				is = new FileInputStream(resource.getFile());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				FileCopyUtils.copy(is, baos);
				StrSubstitutor sub = new StrSubstitutor(this.interpolationValues, "{{", "}}","~".charAt(0));

				this.workflowDefinition = (WORKFLOW) marshaller
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
			
			this.workflowDefinition.setNAME(name);
			
			return this;
		}

		@Override
		public SetInterpolationValue marshaller(Jaxb2Marshaller marshaller) {
			
			this.marshaller=marshaller;
			
			return this;
		}




		@Override
		public SetInterpolationValue setValue(String name, String value) {
			this.interpolationValues.put(name, value);
			return this;
		}




		@Override
		public LoadFromSeedStep noMoreValues() {
			return this;
		}




		@Override
		public BuildStep nameAlreadySet() {
			return this;
		}





		

		
		
	}
	
	
	
	
}
