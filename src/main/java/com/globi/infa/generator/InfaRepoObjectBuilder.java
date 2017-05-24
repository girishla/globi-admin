package com.globi.infa.generator;

import static com.globi.infa.generator.builder.RawStaticFactory.getInstanceFor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.transform.stream.StreamSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.rits.cloning.Cloner;

import lombok.extern.slf4j.Slf4j;
import xjc.ASSOCIATEDSOURCEINSTANCE;
import xjc.CONFIG;
import xjc.CONNECTOR;
import xjc.FOLDER;
import xjc.INSTANCE;
import xjc.MAPPING;
import xjc.MAPPINGVARIABLE;
import xjc.POWERMART;
import xjc.REPOSITORY;
import xjc.SOURCE;
import xjc.SOURCEFIELD;
import xjc.TARGET;
import xjc.TARGETFIELD;
import xjc.TARGETLOADORDER;
import xjc.TRANSFORMATION;
import xjc.TRANSFORMFIELD;
import xjc.WORKFLOW;

@Slf4j
public class InfaRepoObjectBuilder {

	public static PowermartObjectStep newBuilder() {
		return new InfaRepoSteps();
	}

	public interface PowermartObjectStep {
		RepositoryStep powermartObject();
	}

	public interface RepositoryStep {
		SetMarshallerStep repository(REPOSITORY repo);
	}

	public interface SetMarshallerStep {
		FolderStep marshaller(Jaxb2Marshaller marshaller);
	}

	public interface FolderStep {
		ClassStep folder(FOLDER folder);
	}

	public interface ClassStep {
		SourceTableStep simpleTableSyncClass(String simpleTableSyncClass);

		SourceSQLStep sourceSQLSyncClass(String sourceSQLSyncClass);
	}

	public interface SourceTableStep {
		SourceTableStep sourceDefn(SOURCE name);

		TargetDefn noMoreSources();
	}

	public interface TargetDefn {
		TargetDefn targetDefn(TARGET targetDefn);

		MappingStep noMoreTargets();
	}

	public interface SourceSQLStep {
		TransformationStep sourceSQLName(String sourceSQLName);
	}

	public interface MappingStep {
		TransformationStep mappingDefn(MAPPING mapping);

	}

	/*
	 * public interface SourceQualifierStep { SourceQualifierStep
	 * sourceQualifier(String sourceQualifierName);
	 * 
	 * SourceQualifierStep sourceQualifierColumns(String sourceQualifierName,
	 * InfaSourceDefinition sourceDef);
	 * 
	 * SourceQualifierStep sourceQualifierOld(String sourceTable,
	 * List<InfaSourceColumnDefinition> columns);
	 * 
	 * TransformationStep noMoreSourceQualifiers(); }
	 */

	public interface TransformationStep {
		TransformationStep transformation(TRANSFORMATION transformation);

		TransformationStep transformationCopyConnectAllFields(String fromTransformation, String toTransformation);

		TransformationStep transformationField(String transformation, TRANSFORMFIELD field);

		ConnectorStep noMoreTransformations();
	}

	public interface InstanceStep {

		InstanceStep addInstancesForTransformations();

		InstanceStep addInstancesForSources();

		InstanceStep addInstancesForTargets();

		InstanceStep instance(INSTANCE instance);

		ConnectorStep noMoreInstances();
	}

	public interface ConnectorStep {
		ConnectorStep connector(CONNECTOR connector);

		ConnectorStep connector(String fromInstance, String fromField, String toInstance, String toField);

		ConnectorStep autoConnectByName(String fromInstanceName, String toInstanceName);

		TargetLoadOrderStep noMoreConnectors();
	}

	public interface TargetLoadOrderStep {
		TargetLoadOrderStep targetLoadOrderName(TARGETLOADORDER targetLoadOrder);

		MappingVariableStep noMoreTargetLoadOrders();
	}

	public interface MappingVariableStep {
		MappingVariableStep mappingvariable(MAPPINGVARIABLE mappingVariable);

		DefaultConfigStep noMoreMappingVariables();
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

	private static class InfaRepoSteps implements PowermartObjectStep, RepositoryStep, ClassStep, SourceTableStep,
			SourceSQLStep, TargetDefn, BuildStep, MappingVariableStep, TargetLoadOrderStep, ConnectorStep, InstanceStep,
			TransformationStep, MappingStep, FolderStep, DefaultConfigStep, SetMarshallerStep, WorkflowStep {

		@SuppressWarnings("unused")
		private String name;
		@SuppressWarnings("unused")
		private String simpleTableSyncClass;
		@SuppressWarnings("unused")
		private String sourceSQLSyncClass;
		private REPOSITORY repository;
		private List<Object> folderChildren;
		private List<CONNECTOR> connectors;
		private MAPPING mapping;
		private Map<String, TRANSFORMATION> xformMap = new HashMap<>();
		private Map<String, SOURCE> sourceMap = new HashMap<>();
		private Map<String, TARGET> targetMap = new HashMap<>();
		private Jaxb2Marshaller marshaller;
		private POWERMART powermartObject;

		@Override
		public SetMarshallerStep repository(REPOSITORY repo) {
			this.powermartObject.getREPOSITORY().add(repo);
			this.repository = repo;

			return this;
		}

		@Override
		public FolderStep marshaller(Jaxb2Marshaller marshaller) {
			this.marshaller = marshaller;
			return this;
		}

		@Override
		public ClassStep folder(FOLDER folder) {

			this.repository.getFOLDER().add(folder);
			this.folderChildren = folder
					.getFOLDERVERSIONOrCONFIGOrSCHEDULEROrTASKOrSESSIONOrWORKLETOrWORKFLOWOrSOURCEOrTARGETOrTRANSFORMATIONOrMAPPLETOrMAPPINGOrSHORTCUTOrEXPRMACRO();
			return this;
		}

		@Override
		public SourceTableStep simpleTableSyncClass(String simpleTableSyncClass) {
			this.simpleTableSyncClass = simpleTableSyncClass;
			return this;
		}

		@Override
		public SourceSQLStep sourceSQLSyncClass(String sourceSQLSyncClass) {
			this.sourceSQLSyncClass = sourceSQLSyncClass;
			return this;
		}

		@Override
		public SourceTableStep sourceDefn(SOURCE sourceDefn) {

			this.folderChildren.add(sourceDefn);
			sourceMap.put(sourceDefn.getNAME(), sourceDefn);

			return this;
		}

		@Override
		public TargetDefn targetDefn(TARGET targetDefn) {

			this.folderChildren.add(targetDefn);
			targetMap.put(targetDefn.getNAME(), targetDefn);
			return this;
		}

		@Override
		public TransformationStep mappingDefn(MAPPING mapping) {

			this.folderChildren.add(mapping);
			this.mapping = mapping;
			this.connectors = mapping.getCONNECTOR();

			return this;
		}

		@Override
		public TransformationStep transformation(TRANSFORMATION transformation) {

			this.mapping.getTRANSFORMATION().add(transformation);
			xformMap.put(transformation.getNAME(), transformation);
			return this;
		}

		@Override
		public InfaPowermartObject build() {

			// add Source Qualifier Instances here because it needs to be aware
			// of all connectors going into Source Qualifiers
			this.addSourceQualifierInstances();

			InfaPowermartObject powermartObject = new InfaPowermartObject();
			powermartObject.pmObject = this.powermartObject;

			return powermartObject;
		}

		@Override
		public ConnectorStep noMoreTransformations() {

			this.addInstancesForTransformations();
			this.addInstancesForSources();
			this.addInstancesForTargets();

			return this.noMoreInstances();
		}

		@Override
		public InstanceStep instance(INSTANCE instance) {

			this.mapping.getINSTANCE().add(instance);

			return this;
		}

		@Override
		public ConnectorStep noMoreInstances() {

			return this;
		}

		@Override
		public ConnectorStep connector(CONNECTOR connector) {

			return this;
		}

		@Override
		public TargetLoadOrderStep noMoreConnectors() {

			return this;
		}

		@Override
		public MappingVariableStep noMoreTargetLoadOrders() {

			return this;
		}

		@Override
		public DefaultConfigStep noMoreMappingVariables() {

			return this;
		}

		@Override
		public WorkflowStep setdefaultConfigFromSeed(String seedName) throws FileNotFoundException, IOException {

			FileInputStream is = null;

			try {
				Resource resource = new ClassPathResource("seed/" + seedName + ".xml");
				is = new FileInputStream(resource.getFile());
				this.folderChildren.add((CONFIG) this.marshaller.unmarshal(new StreamSource(is)));
			} finally {
				if (is != null) {
					is.close();
				}
			}

			return this;
		}

		@Override
		public TransformationStep sourceSQLName(String sourceSQLName) {

			return this;
		}

		@Override
		public TargetDefn noMoreSources() {
			return this;
		}

		@Override
		public TargetLoadOrderStep targetLoadOrderName(TARGETLOADORDER targetLoadOrder) {

			return this;
		}

		@Override
		public MappingVariableStep mappingvariable(MAPPINGVARIABLE mappingVariable) {

			this.mapping.getMAPPINGVARIABLE().add(mappingVariable);

			return this;
		}

		@Override
		public MappingStep noMoreTargets() {

			return this;
		}

		private List<String> extractFieldNamesForTransformation(String instanceName) {

			return mapping.getTRANSFORMATION().stream()//
					.filter(instance -> instance.getNAME().equals(instanceName))//
					.map(TRANSFORMATION::getTRANSFORMFIELD)//
					.flatMap(List::stream)//
					.map(TRANSFORMFIELD::getNAME)//
					.collect(Collectors.toList());
		}

		private List<String> extractFieldNamesForSources(String instanceName) {

			return sourceMap.entrySet().stream()//
					.filter(instance -> instance.getValue().getNAME().equals(instanceName))//
					.map(Entry::getValue)//
					.map(SOURCE::getSOURCEFIELD)//
					.flatMap(List::stream)//
					.map(SOURCEFIELD::getNAME)//
					.collect(Collectors.toList());

		}

		private List<String> extractFieldNamesForTargets(String instanceName) {

			return targetMap.entrySet().stream()//
					.filter(instance -> instance.getValue().getNAME().equals(instanceName))//
					.map(Entry::getValue)//
					.map(TARGET::getTARGETFIELD)//
					.flatMap(List::stream)//
					.map(TARGETFIELD::getNAME)//
					.collect(Collectors.toList());
		}

		private String getFromInstanceType(String instanceName) {

			if (sourceMap.containsKey(instanceName)) {
				return "Source Definition";

			} else {
				if (xformMap.containsKey(instanceName)) {
					return xformMap.get(instanceName).getTYPE();
				}
			}

			return "";
		}

		private String getToInstanceType(String instanceName) {

			if (targetMap.containsKey(instanceName)) {
				return "Target Definition";

			} else {
				if (xformMap.containsKey(instanceName)) {
					return xformMap.get(instanceName).getTYPE();
				}
			}
			return "";
		}

		@Override
		public ConnectorStep autoConnectByName(String fromInstanceName, String toInstanceName) {

			List<String> fromInstanceFieldNames = new ArrayList<>();
			List<String> toInstanceFieldNames = new ArrayList<>();

			log.debug("Auto-connecting instances " + fromInstanceName + "and " + toInstanceName);

			fromInstanceFieldNames.addAll(extractFieldNamesForTransformation(fromInstanceName));
			toInstanceFieldNames.addAll(extractFieldNamesForTransformation(toInstanceName));

			log.debug("From Fields:");
			fromInstanceFieldNames.forEach(val -> log.debug(val));
			log.debug("To Fields:");
			toInstanceFieldNames.forEach(val -> log.debug(val));

			// It is sufficient to look for fromInstances only in the SourceMap
			// as targets cannot be "From" in a connector.
			fromInstanceFieldNames.addAll(extractFieldNamesForSources(fromInstanceName));
			toInstanceFieldNames.addAll(extractFieldNamesForTargets(toInstanceName));

			List<String> matchingFieldNames = fromInstanceFieldNames.stream().filter(toInstanceFieldNames::contains)
					.collect(Collectors.toList());

			// Add a connector for each matching field
			matchingFieldNames.forEach(matchingField -> {

				CONNECTOR matchingConnector = new CONNECTOR();
				matchingConnector.setFROMFIELD(matchingField);
				matchingConnector.setTOFIELD(matchingField);
				matchingConnector.setFROMINSTANCE(fromInstanceName);
				matchingConnector.setTOINSTANCE(toInstanceName);
				matchingConnector.setFROMINSTANCETYPE(getFromInstanceType(fromInstanceName));
				matchingConnector.setTOINSTANCETYPE(getToInstanceType(toInstanceName));

				mapping.getCONNECTOR().add(matchingConnector);

			});

			return this;
		}

		@Override
		public InstanceStep addInstancesForTransformations() {

			xformMap.forEach((name, transformation) -> {
				if (transformation.getTYPE().equals("Source Qualifier") == false)
					this.mapping.getINSTANCE().add(getInstanceFor(transformation));

			});

			return this;
		}

		@Override
		public InstanceStep addInstancesForSources() {

			sourceMap.forEach((name, source) -> {
				this.mapping.getINSTANCE().add(getInstanceFor(source));

			});

			return this;

		}

		@Override
		public InstanceStep addInstancesForTargets() {

			int[] idx = { 1 };
			targetMap.forEach((name, target) -> {

				this.mapping.getINSTANCE().add(getInstanceFor(target));
				TARGETLOADORDER loadOrder = new TARGETLOADORDER();
				loadOrder.setORDER(Integer.toString(idx[0]++));
				loadOrder.setTARGETINSTANCE(target.getNAME());
				this.mapping.getTARGETLOADORDER().add(loadOrder);

			});

			return this;
		}

		@Override
		public TransformationStep transformationCopyConnectAllFields(String fromTransformation,
				String toTransformation) {

			Cloner cloner=new Cloner();
			
			xformMap.get(fromTransformation).getTRANSFORMFIELD().forEach(field -> {
				TRANSFORMFIELD toField=cloner.deepClone(field);
				toField.setPORTTYPE("INPUT/OUTPUT");
				xformMap.get(toTransformation).getTRANSFORMFIELD().add(toField);
				this.connector(fromTransformation, field.getNAME(), toTransformation, toField.getNAME());

			});
			
			

			// If the target is an expression transform, set the field as an
			// expression by default
			if (xformMap.get(toTransformation).getTYPE().equals("Expression")) {
				xformMap.get(toTransformation).getTRANSFORMFIELD().forEach(field -> {

					if (field.getEXPRESSION() == null || field.getEXPRESSION().isEmpty()) {
						field.setEXPRESSION(field.getNAME());
						field.setEXPRESSIONTYPE("GENERAL");
					}

				});

			}

			return this;
		}

		@Override
		public TransformationStep transformationField(String transformation, TRANSFORMFIELD field) {

			xformMap.get(transformation).getTRANSFORMFIELD().add(field);
			return this;

		}

		@Override
		public ConnectorStep connector(String fromInstance, String fromField, String toInstance, String toField) {

			//remove connectors that have already been linked
			Iterator<CONNECTOR> iter = this.connectors.iterator();
			while (iter.hasNext()) {
				CONNECTOR conn=iter.next();
				if(conn.getTOINSTANCE().equals(toInstance) && conn.getTOFIELD().equals(toField))
				iter.remove();
			}
			
			
			
			CONNECTOR connector = new CONNECTOR();

			connector.setFROMFIELD(fromField);
			connector.setTOFIELD(toField);
			connector.setFROMINSTANCE(fromInstance);
			connector.setTOINSTANCE(toInstance);
			connector.setFROMINSTANCETYPE(getFromInstanceType(fromInstance));
			connector.setTOINSTANCETYPE(getToInstanceType(toInstance));
			mapping.getCONNECTOR().add(connector);

			
			
			
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
			return this;
		}

		private void addSourceQualifierInstances() {

			List<TRANSFORMATION> sourceQualifiers = this.xformMap.entrySet()//
					.stream()//
					.filter(instance -> instance.getValue().getTYPE().equals("Source Qualifier"))//
					.map(Entry::getValue)//
					.collect(Collectors.toList());

			Map<String, String> sourceQualifierSourceInstances = this.connectors.stream()//
					.filter(connector -> sourceQualifiers.stream()//
							.anyMatch(sourceQualifier -> connector.getTOINSTANCE().equals(sourceQualifier.getNAME())))//
					.collect(Collectors.toMap(connector -> connector.getFROMINSTANCE(),
							connector -> connector.getTOINSTANCE(), (toInst, toInstDuplicate) -> toInst));//

			sourceQualifiers.forEach(sQualifierXform -> {

				INSTANCE instance = new INSTANCE();
				instance.setNAME(sQualifierXform.getNAME());
				instance.setDESCRIPTION(sQualifierXform.getDESCRIPTION());
				instance.setTRANSFORMATIONTYPE(sQualifierXform.getTYPE());
				instance.setTRANSFORMATIONNAME(sQualifierXform.getNAME());
				instance.setTYPE("TRANSFORMATION");

				sourceQualifierSourceInstances.entrySet()//
						.stream().filter(entry -> entry.getValue().equals(sQualifierXform.getNAME()))//
						.map(Entry::getKey)
						.forEach(sourceTable -> {
							ASSOCIATEDSOURCEINSTANCE asi = new ASSOCIATEDSOURCEINSTANCE();
							asi.setNAME(sourceTable);
							instance.getASSOCIATEDSOURCEINSTANCE().add(asi);
						});

				this.mapping.getINSTANCE().add(instance);
			});

		}

	}

}
