package com.globi.infa.generator.builder;


import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.util.FileCopyUtils;

import xjc.TRANSFORMATION;

public class SequenceXformBuilder {

	public static SetMarshallerStep newBuilder() {
		return new ExpressionXformSteps();
	}


	public interface SetMarshallerStep {
		SetInterPolationValues marshaller(Jaxb2Marshaller marshaller);
	}

	public interface SetInterPolationValues {
		LoadFromSeedStep setInterpolationValues(Map<String, String> values);
		LoadFromSeedStep noInterpolationValues();
	}
	
	

	public interface LoadFromSeedStep {
		NameStep loadExpressionXformFromSeed(String seedName) throws FileNotFoundException, IOException;
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
		private TRANSFORMATION sequenceXformDefn;
		private Map<String, String> interpolationValues;

		@Override
		public TRANSFORMATION build() {

			return this.sequenceXformDefn;
		}



		@Override
		public NameStep loadExpressionXformFromSeed(String seedName) throws FileNotFoundException, IOException {

			InputStream is = null;

			try {
				Resource resource = new ClassPathResource("seed/" + seedName + ".xml");
				is = resource.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				FileCopyUtils.copy(is, baos);
				StrSubstitutor sub = new StrSubstitutor(interpolationValues, "{{", "}}");

				this.sequenceXformDefn = (TRANSFORMATION) marshaller
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

			this.sequenceXformDefn.setNAME(name);

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


		

	}

}
