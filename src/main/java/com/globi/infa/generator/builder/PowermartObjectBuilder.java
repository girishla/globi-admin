package com.globi.infa.generator.builder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.transform.stream.StreamSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import lombok.extern.slf4j.Slf4j;
import xjc.CONFIG;
import xjc.FOLDER;
import xjc.MAPPING;
import xjc.POWERMART;
import xjc.REPOSITORY;
import xjc.WORKFLOW;

@Slf4j
public class PowermartObjectBuilder {

	public static PowermartObjectStep newBuilder() {
		return new InfaRepoSteps();
	}

	public interface PowermartObjectStep {
		RepositoryStep powermartObject();
	}

	public interface RepositoryStep {
		FolderStep repository(REPOSITORY repo);
	}

	public interface FolderStep {
		SetMarshallerStep folder(FOLDER folder);

		InfaPowermartObject buildPowermartObjWithNoFolders();

	}

	public interface SetMarshallerStep {
		MappingStep marshaller(Jaxb2Marshaller marshaller);

		InfaPowermartObject buildPowermartObjWithBlankFolder();
	}

	public interface MappingStep {
		MappingStep mappingDefn(InfaMappingObject mapping);

		DefaultConfigStep noMoreMappings();

	}

	public interface DefaultConfigStep {
		WorkflowStep setdefaultConfigFromSeed(String seedName) throws FileNotFoundException, IOException;
	}

	public interface WorkflowStep {
		BuildStep workflow(WORKFLOW workflow);
	}

	public interface BuildStep {
		InfaPowermartObject build();

	}

	private static class InfaRepoSteps implements PowermartObjectStep, RepositoryStep, BuildStep, MappingStep,
			FolderStep, DefaultConfigStep, SetMarshallerStep, WorkflowStep {

		@SuppressWarnings("unused")
		private String name;
		@SuppressWarnings("unused")
		private String className;

		private REPOSITORY repository;
		private List<Object> folderChildren;

		private Map<String, MAPPING> mappingMap = new HashMap<>();
		private FOLDER folder;

		private Jaxb2Marshaller marshaller;
		private POWERMART powermartObject;
		private List<InfaFolderObject> folderObjects = new ArrayList<>();

		@Override
		public FolderStep repository(REPOSITORY repo) {
			this.powermartObject.getREPOSITORY().add(repo);
			this.repository = repo;

			return this;
		}

		@Override
		public MappingStep marshaller(Jaxb2Marshaller marshaller) {
			this.marshaller = marshaller;
			return this;
		}

		@Override
		public SetMarshallerStep folder(FOLDER folder) {

			this.repository.getFOLDER().add(folder);
			this.folder = folder;
			this.folderChildren = folder
					.getFOLDERVERSIONOrCONFIGOrSCHEDULEROrTASKOrSESSIONOrWORKLETOrWORKFLOWOrSOURCEOrTARGETOrTRANSFORMATIONOrMAPPLETOrMAPPINGOrSHORTCUTOrEXPRMACRO();

			return this;
		}

		@Override
		public InfaPowermartObject build() {

			InfaPowermartObject powermartObject = new InfaPowermartObject();
			powermartObject.pmObject = this.powermartObject;
			powermartObject.folderObjects = this.folderObjects;
			powermartObject.folderName = this.folder.getNAME();

			return powermartObject;
		}

		@Override
		public WorkflowStep setdefaultConfigFromSeed(String seedName) throws FileNotFoundException, IOException {

			InputStream is = null;

			try {
				Resource resource = new ClassPathResource("seed/" + seedName + ".xml");
				is = resource.getInputStream();
				CONFIG configObj = (CONFIG) this.marshaller.unmarshal(new StreamSource(is));
				this.folderChildren.add(configObj);
				this.folderObjects.add(new InfaConfigObject(configObj));

			} finally {
				if (is != null) {
					is.close();
				}
			}

			return this;
		}

		@Override
		public RepositoryStep powermartObject() {
			this.powermartObject = new POWERMART();
			return this;
		}

		@Override
		public BuildStep workflow(WORKFLOW workflow) {
			this.folderChildren.add(workflow);
			this.folderObjects.add(new InfaWorkflowObject(workflow));
			return this;
		}

		@Override
		public InfaPowermartObject buildPowermartObjWithBlankFolder() {
			InfaPowermartObject powermartObject = new InfaPowermartObject();
			powermartObject.pmObject = this.powermartObject;
			powermartObject.folderName = this.folder.getNAME();
			return powermartObject;
		}

		@Override
		public InfaPowermartObject buildPowermartObjWithNoFolders() {
			InfaPowermartObject powermartObject = new InfaPowermartObject();
			powermartObject.pmObject = this.powermartObject;

			return powermartObject;
		}

		@Override
		public DefaultConfigStep noMoreMappings() {

			return this;
		}

		@Override
		public MappingStep mappingDefn(InfaMappingObject mapping) {

			this.folderObjects.addAll(mapping.getFolderObjects());
			this.mappingMap.put(mapping.getMapping().getNAME(), mapping.getMapping());

			List<InfaFolderObject> folderObjects = mapping.getFolderObjects();

			List<Object> folderObject = folderObjects.stream()//
					.map(fo -> fo.getFolderObj())//
					.collect(Collectors.toList());

			this.folderChildren.addAll(folderObject);

			return this;
		}

	}

}
