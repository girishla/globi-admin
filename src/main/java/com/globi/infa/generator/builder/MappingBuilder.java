package com.globi.infa.generator.builder;

import static com.globi.infa.generator.InfaGeneratorDefaults.DEFAULT_DESCRIPTION;
import static com.globi.infa.generator.InfaGeneratorDefaults.DEFAULT_VERSION;
import static com.globi.infa.generator.builder.InfaObjectMother.getInstanceFor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.globi.infa.datasource.core.ObjectNameNormaliser;
import com.globi.infa.metadata.src.SILInfaSourceColumnDefinition;
import com.rits.cloning.Cloner;

import lombok.extern.slf4j.Slf4j;
import xjc.ASSOCIATEDSOURCEINSTANCE;
import xjc.CONNECTOR;
import xjc.INSTANCE;
import xjc.MAPPING;
import xjc.MAPPINGVARIABLE;
import xjc.MAPPLET;
import xjc.SOURCE;
import xjc.SOURCEFIELD;
import xjc.TARGET;
import xjc.TARGETFIELD;
import xjc.TARGETLOADORDER;
import xjc.TRANSFORMATION;
import xjc.TRANSFORMFIELD;

@Slf4j
public class MappingBuilder {

	public static ClassStep newBuilder() {
		return new InfaMappingSteps();
	}

	public interface ClassStep {
		SourceTableStep simpleTableSyncClass(String simpleTableSyncClass);

		SourceTableStep primaryExtractClass(String primaryExtractClass);

		SourceSQLStep sourceSQLSyncClass(String sourceSQLSyncClass);
	}

	public interface SourceTableStep {
		SourceTableStep sourceDefn(SOURCE name);

		TargetDefn noMoreSources();
	}

	public interface TargetDefn {
		TargetDefn targetDefn(TARGET targetDefn);

		MappletStep noMoreTargets();
	}

	public interface SourceSQLStep {
		TransformationStep sourceSQLName(String sourceSQLName);
	}

	public interface MappletStep {
		MappletStep mappletDefn(MAPPLET mapplet);

		ReusableTransformationStep noMoreMapplets();

	}

	public interface ReusableTransformationStep {
		ReusableTransformationStep reusableTransformation(TRANSFORMATION xform);

		ReusableTransformationStep reusableTransformations(List<TRANSFORMATION> xforms);

		MappingStep noMoreReusableXforms();

	}

	public interface MappingStep {
		TransformationStep startMappingDefn(String mappingName);

	}

	public interface TransformationStep {
		TransformationStep transformation(TRANSFORMATION transformation);

		TransformationStep transformationCopyConnectAllFields(String fromTransformation, String toTransformation);

		TransformationStep transformationField(String transformation, TRANSFORMFIELD field);

		TransformationStep connector(CONNECTOR connector);

		TransformationStep connector(String fromInstance, String fromField, String toInstance, String toField);

		TransformationStep autoConnectByName(String fromInstanceName, String toInstanceName);

		TransformationStep autoConnectByTransformedName(String fromInstanceName, String toInstanceName,
				UnaryOperator<String> op);

		ConnectorStep noMoreTransformations();
	}

	public interface InstanceStep {

		InstanceStep addInstancesForTransformations();

		InstanceStep addInstancesForMapplets();

		InstanceStep addInstancesForSources();

		InstanceStep addInstancesForTargets();

		InstanceStep instance(INSTANCE instance);

		ConnectorStep noMoreInstances();
	}

	public interface ConnectorStep {

		TargetLoadOrderStep noMoreConnectors();
	}

	public interface TargetLoadOrderStep {
		TargetLoadOrderStep targetLoadOrderName(TARGETLOADORDER targetLoadOrder);

		MappingVariableStep noMoreTargetLoadOrders();
	}

	public interface MappingVariableStep {
		MappingVariableStep mappingvariable(MAPPINGVARIABLE mappingVariable);

		BuildStep noMoreMappingVariables();
	}

	public interface BuildStep {
		InfaMappingObject build();

	}

	private static class InfaMappingSteps implements ClassStep, SourceTableStep, SourceSQLStep, TargetDefn, MappletStep,
			ReusableTransformationStep, BuildStep, MappingVariableStep, TargetLoadOrderStep, ConnectorStep,
			InstanceStep, TransformationStep, MappingStep {

		@SuppressWarnings("unused")
		private String name;
		@SuppressWarnings("unused")
		private String className;

		private List<CONNECTOR> connectors;
		private MAPPING mapping;
		private Map<String, TRANSFORMATION> xformMap = new HashMap<>();
		private Map<String, SOURCE> sourceMap = new HashMap<>();
		private Map<String, TARGET> targetMap = new HashMap<>();
		private Map<String, MAPPLET> mappletMap = new HashMap<>();
		private List<InfaFolderObject> folderObjects = new ArrayList<>();
		private List<Object> genericFolderChildren = new ArrayList<>();

		@Override
		public SourceTableStep simpleTableSyncClass(String simpleTableSyncClass) {
			this.className = simpleTableSyncClass;
			return this;
		}

		@Override
		public SourceSQLStep sourceSQLSyncClass(String sourceSQLSyncClass) {
			this.className = sourceSQLSyncClass;
			return this;
		}

		@Override
		public SourceTableStep primaryExtractClass(String primaryExtractClass) {
			this.className = primaryExtractClass;
			return this;
		}

		@Override
		public SourceTableStep sourceDefn(SOURCE sourceDefn) {

			this.folderObjects.add(new InfaSourceObject(sourceDefn));

			sourceMap.put(sourceDefn.getNAME(), sourceDefn);

			return this;
		}

		@Override
		public TargetDefn targetDefn(TARGET targetDefn) {

			this.folderObjects.add(new InfaTargetObject(targetDefn));

			targetMap.put(targetDefn.getNAME(), targetDefn);
			return this;
		}

		@Override
		public TransformationStep startMappingDefn(String mappingName) {

			MAPPING mapping = new MAPPING();

			mapping.setDESCRIPTION(DEFAULT_DESCRIPTION.getValue());
			mapping.setNAME(mappingName);
			mapping.setOBJECTVERSION(DEFAULT_VERSION.getValue());
			mapping.setVERSIONNUMBER(DEFAULT_VERSION.getValue());
			mapping.setISVALID("YES");

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
		public ConnectorStep noMoreTransformations() {

			this.addInstancesForTransformations();
			this.addInstancesForSources();
			this.addInstancesForTargets();
			this.addInstancesForMapplets();

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
		public TransformationStep connector(CONNECTOR connector) {

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
		public MappletStep noMoreTargets() {

			return this;
		}

		private List<String> extractInputFieldNamesForTransformation(String instanceName) {

			if (xformMap.containsKey(instanceName)) {

				log.info("found TO instance -- " + instanceName);

				return xformMap.get(instanceName).getTRANSFORMFIELD().stream()//
						.filter(transformField -> (transformField.getPORTTYPE().equals("INPUT")
								|| transformField.getPORTTYPE().equals("INPUT/OUTPUT")))
						.map(TRANSFORMFIELD::getNAME)//
						.collect(Collectors.toList());
			}

			return mapping.getTRANSFORMATION().stream()//
					.filter(instance -> instance.getNAME().equals(instanceName))//
					.map(TRANSFORMATION::getTRANSFORMFIELD)//
					.flatMap(List::stream)//
					.filter(transformField -> (transformField.getPORTTYPE().equals("INPUT")
							|| transformField.getPORTTYPE().equals("INPUT/OUTPUT")))
					.map(TRANSFORMFIELD::getNAME)//
					.collect(Collectors.toList());

		}

		private List<String> extractFieldNamesForTransformation(String instanceName) {

			if (xformMap.containsKey(instanceName)) {

				// to deal with reusable xforms - defn wont be part of mapping
				return xformMap.get(instanceName).getTRANSFORMFIELD().stream()//
						.filter(transformField -> transformField.getPORTTYPE().endsWith("OUTPUT"))
						.map(TRANSFORMFIELD::getNAME)//
						.collect(Collectors.toList());

			}

			return mapping.getTRANSFORMATION().stream()//
					.filter(instance -> instance.getNAME().equals(instanceName))//
					.map(TRANSFORMATION::getTRANSFORMFIELD)//
					.flatMap(List::stream)//
					.filter(transformField -> transformField.getPORTTYPE().endsWith("OUTPUT"))
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

			} else if (mappletMap.containsKey(instanceName)) {
				return "Mapplet";

			} else if (targetMap.containsKey(instanceName)) {
				return "Target Definition";
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

			} else if (mappletMap.containsKey(instanceName)) {
				return "Mapplet";

			} else {
				if (xformMap.containsKey(instanceName)) {
					return xformMap.get(instanceName).getTYPE();
				}
			}
			return "";
		}

		@Override
		public TransformationStep autoConnectByName(String fromInstanceName, String toInstanceName) {

			List<String> fromInstanceFieldNames = new ArrayList<>();
			List<String> toInstanceFieldNames = new ArrayList<>();
			Map<String, String> normalisedColumns = new HashMap<>();

			log.debug("Auto-connecting instances " + fromInstanceName + "and " + toInstanceName);

			fromInstanceFieldNames.addAll(extractFieldNamesForTransformation(fromInstanceName));
			toInstanceFieldNames.addAll(extractInputFieldNamesForTransformation(toInstanceName));

			// It is sufficient to look for fromInstances only in the SourceMap
			// as targets cannot be "From" in a connector.
			fromInstanceFieldNames.addAll(extractFieldNamesForSources(fromInstanceName));

			// save normalised column names as Hash to use later
			fromInstanceFieldNames//
					.stream()//
					.forEach(col -> normalisedColumns.put(ObjectNameNormaliser.normalise(col), col));

			// collect normalise column names as list before comparing
			fromInstanceFieldNames = fromInstanceFieldNames//
					.stream()//
					.map(ObjectNameNormaliser::normalise)//
					.collect(Collectors.toList());

			toInstanceFieldNames.addAll(extractFieldNamesForTargets(toInstanceName));

			List<String> matchingFieldNames = fromInstanceFieldNames.stream()//
					.filter(toInstanceFieldNames::contains).collect(Collectors.toList());

			// Add a connector for each matching field
			matchingFieldNames.forEach(matchingField -> {
				this.connector(fromInstanceName, normalisedColumns.get(matchingField), toInstanceName, matchingField);

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
		public InstanceStep addInstancesForMapplets() {

			mappletMap.forEach((name, mapplet) -> {
				this.mapping.getINSTANCE().add(getInstanceFor(mapplet));

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

			Cloner cloner = Cloner.shared();

			xformMap.get(fromTransformation).getTRANSFORMFIELD().forEach(field -> {
				TRANSFORMFIELD toField = cloner.deepClone(field);
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
		public TransformationStep connector(String fromInstance, String fromField, String toInstance, String toField) {

			// remove connectors that have already been linked
			Iterator<CONNECTOR> iter = this.connectors.iterator();
			while (iter.hasNext()) {
				CONNECTOR conn = iter.next();
				if (conn.getTOINSTANCE().equals(toInstance) && conn.getTOFIELD().equals(toField))
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

		private List<TRANSFORMATION> getAllSourceQualifiers() {

			return this.xformMap.entrySet()//
					.stream()//
					.filter(instance -> instance.getValue().getTYPE().equals("Source Qualifier"))//
					.map(Entry::getValue)//
					.collect(Collectors.toList());
		}

		private Map<String, String> usingConnectorListFindSourceInstancesFor(List<TRANSFORMATION> sourceQualifiers) {

			return this.connectors.stream()//
					.filter(connector -> sourceQualifiers.stream()//
							.anyMatch(sourceQualifier -> connector.getTOINSTANCE().equals(sourceQualifier.getNAME())))//
					.collect(Collectors.toMap(connector -> connector.getFROMINSTANCE(),
							connector -> connector.getTOINSTANCE(), (toInst, toInstDuplicate) -> toInst));//

		}

		private void addSourceQualifierInstances() {

			List<TRANSFORMATION> sourceQualifiers = getAllSourceQualifiers();
			Map<String, String> sourceQualifierSourceInstances = usingConnectorListFindSourceInstancesFor(
					sourceQualifiers);

			sourceQualifiers.forEach(sQualifierXform -> {

				INSTANCE instance = new INSTANCE();
				instance.setNAME(sQualifierXform.getNAME());
				instance.setDESCRIPTION(sQualifierXform.getDESCRIPTION());
				instance.setTRANSFORMATIONTYPE(sQualifierXform.getTYPE());
				instance.setTRANSFORMATIONNAME(sQualifierXform.getNAME());
				instance.setTYPE("TRANSFORMATION");

				sourceQualifierSourceInstances.entrySet()//
						.stream().filter(entry -> entry.getValue().equals(sQualifierXform.getNAME()))//
						.map(Entry::getKey).forEach(sourceTable -> {
							ASSOCIATEDSOURCEINSTANCE asi = new ASSOCIATEDSOURCEINSTANCE();
							asi.setNAME(sourceTable);
							instance.getASSOCIATEDSOURCEINSTANCE().add(asi);
						});

				this.mapping.getINSTANCE().add(instance);
			});

		}

		@Override
		public MappletStep mappletDefn(MAPPLET mapplet) {
			this.folderObjects.add(new InfaMappletObject(mapplet));
			mappletMap.put(mapplet.getNAME(), mapplet);
			return this;
		}

		@Override
		public ReusableTransformationStep noMoreMapplets() {
			return this;
		}

		@Override
		public BuildStep noMoreMappingVariables() {
			return this;
		}

		@Override
		public InfaMappingObject build() {

			// add Source Qualifier Instances here because it needs to be aware
			// of all connectors going into Source Qualifiers
			this.addSourceQualifierInstances();
			InfaMappingObject mappingObject = new InfaMappingObject(this.mapping);
			this.folderObjects.add(mappingObject);
			mappingObject.setFolderObjects(this.folderObjects);
			return mappingObject;
		}

		@Override
		public TransformationStep autoConnectByTransformedName(String fromInstanceName, String toInstanceName,
				UnaryOperator<String> transformationFunction) {

			List<String> fromInstanceFieldNames = new ArrayList<>();
			List<String> toInstanceFieldNames = new ArrayList<>();
			Map<String, String> normalisedColumns = new HashMap<>();

			log.debug("Auto-connecting instances transformed " + fromInstanceName + " and " + toInstanceName);

			fromInstanceFieldNames.addAll(extractFieldNamesForTransformation(fromInstanceName));
			toInstanceFieldNames.addAll(extractInputFieldNamesForTransformation(toInstanceName));

			// It is sufficient to look for fromInstances only in the SourceMap
			// as targets cannot be "From" in a connector.
			fromInstanceFieldNames.addAll(extractFieldNamesForSources(fromInstanceName));

			// save normalised column names as Hash to use later
			fromInstanceFieldNames//
					.stream()//
					.forEach(col -> normalisedColumns.put(ObjectNameNormaliser.normalise(col), col));

			// collect normalise column names as list before comparing
			fromInstanceFieldNames = fromInstanceFieldNames//
					.stream()//
					.map(ObjectNameNormaliser::normalise)//
					.collect(Collectors.toList());

			toInstanceFieldNames.addAll(extractFieldNamesForTargets(toInstanceName));

			List<String> matchingFieldNames = fromInstanceFieldNames.stream()//
					.filter(fromField -> toInstanceFieldNames.indexOf(transformationFunction.apply(fromField)) != -1)//
					.collect(Collectors.toList());

			// Add a connector for each matching field
			matchingFieldNames.forEach(matchingField -> {
				this.connector(fromInstanceName, normalisedColumns.get(matchingField), toInstanceName,
						transformationFunction.apply(matchingField));

			});

			return this;

		}

		@Override
		public ReusableTransformationStep reusableTransformation(TRANSFORMATION xform) {
			this.folderObjects.add(new InfaTransformationObject(xform));
			xformMap.put(xform.getNAME(), xform);
			return this;

		}

		

		@Override
		public MappingStep noMoreReusableXforms() {

			return this;
		}

		@Override
		public ReusableTransformationStep reusableTransformations(List<TRANSFORMATION> xforms) {

			xforms.forEach(xform -> {

						this.folderObjects.add(new InfaTransformationObject(xform));
						xformMap.put(xform.getNAME(), xform);

					});

			return this;
		}

	}

}
