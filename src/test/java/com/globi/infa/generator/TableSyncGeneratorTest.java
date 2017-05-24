package com.globi.infa.generator;

import static com.globi.infa.generator.factory.RawStaticFactory.getEtlProcWidMappingVariable;
import static com.globi.infa.generator.factory.RawStaticFactory.getFolderFor;
import static com.globi.infa.generator.factory.RawStaticFactory.getInitialExtractDateMappingVariable;
import static com.globi.infa.generator.factory.RawStaticFactory.getMappingFrom;
import static com.globi.infa.generator.factory.RawStaticFactory.getRepository;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.xml.sax.SAXException;

import com.globi.AbstractIntegrationTest;
import com.globi.infa.datasource.core.OracleToInfaDataTypeMapper;
import com.globi.infa.datasource.gen.GENTableColumnRepository;
import com.globi.infa.datasource.lnicrm.LNICRMTableColumnRepository;
import com.globi.infa.generator.factory.ExpressionXformBuilder;
import com.globi.infa.generator.factory.FilterXformBuilder;
import com.globi.infa.generator.factory.LookupXformBuilder;
import com.globi.infa.generator.factory.SequenceXformBuilder;
import com.globi.infa.generator.factory.SourceDefinitionBuilder;
import com.globi.infa.generator.factory.SourceQualifierBuilder;
import com.globi.infa.generator.factory.TargetDefinitionBuilder;
import com.globi.infa.generator.factory.WorkflowDefinitionBuilder;
import com.globi.infa.sourcedefinition.InfaSourceDefinition;

import xjc.POWERMART;

public class TableSyncGeneratorTest extends AbstractIntegrationTest {

	@Autowired
	LNICRMTableColumnRepository lnicrmColumnRepository;

	@Autowired
	GENTableColumnRepository genColumnRepository;

	InfaSourceDefinition sourceTableDef;

	@Autowired
	OracleToInfaDataTypeMapper oraDataTypemapper;

	@Autowired
	private Jaxb2Marshaller marshaller;

	Map<String, String> emptyValuesMap = new HashMap<>();
	Map<String, String> lookupXformValuesMap = new HashMap<>();

	final String sourceQualifierFilterClauseColumn = "LAST_UPD";

	private static final String FILE_NAME = "c:\\temp\\output_file.xml";

	@Before
	public void setup() {

		sourceTableDef = InfaSourceDefinition.builder().sourceTableName("S_ORG_EXT")//
				.ownerName("SIEBEL")//
				.databaseName("CUK")//
				.databaseType("Oracle")//
				.build();

		sourceTableDef.getColumns().addAll(lnicrmColumnRepository.getAllColumnsFor("S_ORG_EXT"));
		sourceTableDef.getColumns().forEach(column -> {
			if (column.getColumnName().equals("ROW_ID")) {
				column.setIntegrationIdFlag(true);
			}

		});

		lookupXformValuesMap.put("targetTableName",
				sourceTableDef.getDatabaseName() + "_" + sourceTableDef.getSourceTableName());

	}

	private void saveXML(Object jaxbObject) throws IOException {
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(FILE_NAME);
			this.marshaller.marshal(jaxbObject, new StreamResult(os));
		} finally {
			if (os != null) {
				os.close();
			}
		}
	}

	private String asString(JAXBContext pContext, Object pObject) throws JAXBException {

		java.io.StringWriter sw = new StringWriter();

		Marshaller jaxMarshaller = pContext.createMarshaller();
		jaxMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		jaxMarshaller.marshal(pObject, sw);

		return sw.toString();
	}

	private POWERMART loadControlFileAsObject(String controlFileName) throws FileNotFoundException, IOException {

		FileInputStream is = null;

		try {
			Resource resource = new ClassPathResource("test/" + controlFileName + ".xml");
			is = new FileInputStream(resource.getFile());
			return (POWERMART) marshaller.unmarshal(new StreamSource(is));
		} finally {
			if (is != null) {
				is.close();
			}
		}

	}

	@Test
	public void generatePTPWorkflowFromSourcetable() throws IOException, SAXException, JAXBException {

		InfaPowermartObject pmObj = InfaRepoObjectBuilder//
				.newBuilder()//
				.powermartObject().repository(getRepository())//
				.marshaller(marshaller)//
				.folder(getFolderFor("LAW_PTP_" + sourceTableDef.getDatabaseName(), "Pull to puddle folder"))//
				.simpleTableSyncClass("simpleTableSyncClass")//
				.sourceDefn(SourceDefinitionBuilder.newBuilder()//
						.sourceDefnFromPrototype("SourceFromPrototype")//
						.sourceDefn(sourceTableDef)//
						.addFields(sourceTableDef.getColumns())//
						.name(sourceTableDef.getSourceTableName())//
						.build())
				.noMoreSources()//
				.targetDefn(TargetDefinitionBuilder.newBuilder()//
						.marshaller(marshaller)//
						.loadTargetFromSeed("Seed_PTPTargetTableSystemCols")//
						.addFields(sourceTableDef.getColumns())//
						.name(sourceTableDef.getDatabaseName() + "_" + sourceTableDef.getSourceTableName())//
						.build())//
				.noMoreTargets()//
				.mappingDefn(getMappingFrom(sourceTableDef))//
				.transformation(SourceQualifierBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setValue("sourceFilter",
								sourceTableDef.getSourceTableName() + "." + sourceQualifierFilterClauseColumn
										+ " >= TO_DATE('$$INITIAL_EXTRACT_DATE','dd/MM/yyyy HH24:mi:ss')")
						.noMoreValues().loadSourceQualifierFromSeed("Seed_SourceQualifier")//
						.addFields(oraDataTypemapper, sourceTableDef.getColumns())//
						.name("SQ_ExtractData")//
						.build())//
				.transformation(ExpressionXformBuilder.newBuilder()//
						.expressionFromPrototype("ExpFromPrototype")//
						.expression("EXP_Resolve")//
						.addEffectiveFromDateField()//
						.addEtlProcWidField()//
						.addIntegrationIdField(sourceTableDef.getColumns())//
						.addMD5HashField(sourceTableDef.getColumns())//
						.addRowWidField()//
						.noMoreFields()//
						.nameAlreadySet()//
						.build())//
				.transformationCopyConnectAllFields("SQ_ExtractData", "EXP_Resolve")//
				.transformation(SequenceXformBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setInterpolationValues(emptyValuesMap)//
						.loadExpressionXformFromSeed("Seed_WidSequence")//
						.nameAlreadySet()//
						.build())
				.transformation(LookupXformBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setInterpolationValues(lookupXformValuesMap)//
						.loadExpressionXformFromSeed("Seed_LKPRecordInstanceViaHash")//
						.nameAlreadySet()//
						.build())
				.transformation(FilterXformBuilder.newBuilder()//
						.filterFromPrototype("FilterFromPrototype")//
						.filter("FIL_ChangesOnly")//
						.noMoreFields()//
						.addCondition("ISNULL(HASH_RECORD)")//
						.noMoreConditions()//
						.nameAlreadySet()//
						.build())
				.transformationCopyConnectAllFields("EXP_Resolve", "FIL_ChangesOnly")//
				.noMoreTransformations()//
				.autoConnectByName(sourceTableDef.getSourceTableName(), "SQ_ExtractData")//
				.autoConnectByName("FIL_ChangesOnly",
						sourceTableDef.getDatabaseName() + "_" + sourceTableDef.getSourceTableName())//
				.connector("SEQ_WID", "NEXTVAL", "EXP_Resolve", "ROW_WID")
				.connector("EXP_Resolve", "HASH_RECORD", "LKP_RecordInstance", "HASH_RECORD_IN")
				.connector("LKP_RecordInstance", "HASH_RECORD", "FIL_ChangesOnly", "HASH_RECORD")//
				.noMoreConnectors()//
				.noMoreTargetLoadOrders()//
				.mappingvariable(getEtlProcWidMappingVariable())//
				.mappingvariable(getInitialExtractDateMappingVariable()).noMoreMappingVariables()//
				.setdefaultConfigFromSeed("Seed_DefaultSessionConfig")//
				.workflow(WorkflowDefinitionBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setValue("phasePrefix", "PTP")//
						.setValue("primaryName", sourceTableDef.getSourceTableName())//
						.setValue("sourceShortCode", sourceTableDef.getDatabaseName())//
						.setValue("TargetShortCode", "LAW")//
						.noMoreValues()//
						.loadWorkflowFromSeed("Seed_SimpleWorkflow")//
						.nameAlreadySet()//
						.build())//
				.build();

		saveXML(pmObj.pmObject);
		
		
		String testXML = asString(marshaller.getJaxbContext(), pmObj.pmObject);
		POWERMART controlObj = loadControlFileAsObject("CONTROL_PTP_CUK_S_ORG_EXT_Extract");
		String controlXML = asString(marshaller.getJaxbContext(), controlObj);

		assertXMLEqual(controlXML, testXML);


	}

}
