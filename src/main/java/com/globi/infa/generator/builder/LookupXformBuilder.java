package com.globi.infa.generator.builder;


import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.util.FileCopyUtils;


import xjc.TRANSFORMATION;

public class LookupXformBuilder {

	public static SetMarshallerStep newBuilder() {
		return new ExpressionXformSteps();
	}


	public interface SetMarshallerStep {
		SetInterPolationValues marshaller(Jaxb2Marshaller marshaller);
	}

	public interface SetInterPolationValues {
		SetInterPolationValues setValue(String name, String value);
		LoadFromSeedStep setInterpolationValues(Map<String, String> values);
		LoadFromSeedStep noMoreInterpolationValues();

	}
	
	

	public interface LoadFromSeedStep {
		NameStep loadLookupXformFromSeed(String seedName) throws FileNotFoundException, IOException;
	}



	public interface NameStep {
		BuildStep name(String name);
		BuildStep nameAlreadySet();
	}

	public interface BuildStep {
		TRANSFORMATION build();
	}

	public static class ExpressionXformSteps implements  NameStep, SetMarshallerStep,
			SetInterPolationValues, LoadFromSeedStep, BuildStep {

		private Jaxb2Marshaller marshaller;
		private TRANSFORMATION lookupXformDefn;
		private Map<String, String> interpolationValues = new HashMap<>();

		@Override
		public TRANSFORMATION build() {

			return this.lookupXformDefn;
		}



		@Override
		public NameStep loadLookupXformFromSeed(String seedName) throws FileNotFoundException, IOException {

			InputStream is = null;

			try {
				Resource resource = new ClassPathResource("seed/" + seedName + ".xml");
				is = resource.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				FileCopyUtils.copy(is, baos);
				StrSubstitutor sub = new StrSubstitutor(interpolationValues, "{{", "}}");

				this.lookupXformDefn = (TRANSFORMATION) marshaller
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

			this.lookupXformDefn.setNAME(name);

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
		public LoadFromSeedStep noMoreInterpolationValues() {
			return this;
		}

		@Override
		public BuildStep nameAlreadySet() {
		
			return this;
		}
		
		
		@Override
		public SetInterPolationValues setValue(String name, String value) {
			this.interpolationValues.put(name, value);
			return this;
		}

		

	}

}
