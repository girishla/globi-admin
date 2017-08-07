package com.globi.infa.generator.sil;

import static com.globi.infa.generator.CommonStaticObjectMother.getInfaSourceColumnsFromSourceDefn;
import static com.globi.infa.generator.sil.SILStaticObjectMother.*;
import static com.globi.infa.generator.sil.SILStaticObjectMother.getMeasureAttribColumn;
import static com.globi.infa.generator.sil.SILStaticObjectMother.getMeasureColumn;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.globi.infa.InfaConfig;
import com.globi.infa.datasource.core.DataSourceTableDTO;
import com.globi.infa.datasource.core.DataTypeMapper;
import com.globi.infa.datasource.type.oracle.OracleInfaSourceToInfaTargetTypeMapper;
import com.globi.infa.datasource.type.oracle.OracleInfaSourceToInfaXFormTypeMapper;
import com.globi.infa.generator.builder.InfaMappingObject;
import com.globi.infa.generator.builder.SourceDefinitionBuilder;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.metadata.src.SILInfaSourceColumnDefinition;
import com.globi.infa.workflow.SILWorkflow;
import com.globi.infa.workflow.SILWorkflowSourceColumn;
import com.globi.metadata.sourcesystem.SourceSystem;

import lombok.extern.slf4j.Slf4j;
import xjc.CONNECTOR;
import xjc.INSTANCE;
import xjc.SOURCE;
import xjc.TARGET;
import xjc.TRANSFORMATION;
import xjc.TRANSFORMFIELD;

@Slf4j
public class SILFactMappingGeneratorTest {

	private DataTypeMapper sourcetoXformDataTypeMapper;
	private DataTypeMapper sourceToTargetDatatypeMapper;
	private SourceSystem sourceSystem;
	private DataSourceTableDTO sourceTable;
	private Jaxb2Marshaller marshaller;
	private SILWorkflow silWorkflow;
	private List<InfaSourceColumnDefinition> allSourceColumns;
	private List<SILInfaSourceColumnDefinition> matchedColumnsSIL;
	private List<SILInfaSourceColumnDefinition> allTargetColumns;
	private List<SILInfaSourceColumnDefinition> allOneToOneDimColumns;

	private static final String STG_TABLE_INVOICE_LN = "X_INVOICE_LN";
	private static final String TABLE_INVOICE_LN = "INVOICE_LN";
	private static final String SOURCE_NAME_LAW = "LAW";
	private static final String TABLE_OWNER_LAW = "LAW";
	private static final String DB_TYPE_ORACLE = "Oracle";
	private static final int SOURCE_NUM_LAW = 1;

	private SILFactMappingGenerator mappingService;

	@Before
	public void setUp() throws Exception {

		sourcetoXformDataTypeMapper = new OracleInfaSourceToInfaXFormTypeMapper();
		sourceToTargetDatatypeMapper = new OracleInfaSourceToInfaTargetTypeMapper();
		sourceSystem = SourceSystem.builder()//
				.dbName(SOURCE_NAME_LAW)//
				.dbType(DB_TYPE_ORACLE)//
				.name(SOURCE_NAME_LAW)//
				.ownerName(TABLE_OWNER_LAW)//
				.sourceNum(SOURCE_NUM_LAW)//
				.build();

		marshaller = new InfaConfig().jaxb2Marshaller();

		List<SILWorkflowSourceColumn> cols = new ArrayList<>();
		cols.add(SILWorkflowSourceColumn.builder()//
				.columnName("ROW_WID")//
				.autoColumn(true)//
				.columnType("Primary Key")//
				.domainLookupColumn(false)//
				.legacyColumn(true)//
				.miniDimColumn(true)//
				.stageTableColumn(false).targetColumn(true)//
				.build());
		cols.add(SILWorkflowSourceColumn.builder()//
				.columnName("DATASOURCE_NUM_ID")//
				.autoColumn(true)//
				.columnType("System")//
				.domainLookupColumn(false)//
				.legacyColumn(true)//
				.miniDimColumn(true)//
				.stageTableColumn(true).targetColumn(false)//
				.build());
		cols.add(getMeasureAttribColumn("DOC_CURCY_CD"));
		cols.add(getMeasureColumn("DOC_AMT"));
		cols.add(getFKWIDColumn("AGR_WID", "AGR"));
		cols.add(getFKWIDColumn("BU_WID", "BU"));
		cols.add(SILWorkflowSourceColumn.builder()//
				.columnName("ORIG_INV_NUM")//
				.autoColumn(true)//
				.columnType("Attribute")//
				.domainLookupColumn(false)//
				.legacyColumn(true)//
				.miniDimColumn(true)//
				.stageTableColumn(true).targetColumn(false)//
				.build());
		cols.add(SILWorkflowSourceColumn.builder()//
				.columnName("SRC_BILL_EVT")//
				.autoColumn(true)//
				.columnType("Attribute")//
				.domainLookupColumn(false)//
				.legacyColumn(true)//
				.miniDimColumn(true)//
				.stageTableColumn(true).targetColumn(false)//
				.build());
		cols.add(SILWorkflowSourceColumn.builder()//
				.columnName("BU_PGUID")//
				.autoColumn(true)//
				.columnType("Foreign Key")//
				.domainLookupColumn(false)//
				.legacyColumn(true)//
				.miniDimColumn(true)//
				.stageTableColumn(true).targetColumn(false)//
				.build());

		silWorkflow = SILWorkflow.builder()//
				.loadType("Fact")//
				.stageName(STG_TABLE_INVOICE_LN)//
				.tableName(TABLE_INVOICE_LN).columns(cols)//
				.workflowUri("")//
				.workflowName("SIL_" + STG_TABLE_INVOICE_LN + "_Fact")//
				.build();

		allSourceColumns = getInfaSourceColumnsFromSourceDefn(SourceDefinitionBuilder//
				.newBuilder()//
				.sourceFromSeed("seedClass")//
				.marshaller(marshaller)//
				.loadSourceFromSeed("TEST_SEED_SOURCE_INVOICELN")//
				.noFields()//
				.name(STG_TABLE_INVOICE_LN)//
				.build()//
		);

		matchedColumnsSIL = SILGeneratorContext.getFilteredSourceDefnColumns(allSourceColumns,
				silWorkflow.getColumns());

		allOneToOneDimColumns = matchedColumnsSIL.stream()//
				.filter(col -> col.getColumnType().equals("Attribute")
						|| col.getColumnType().equals("Measure Attribute"))//
				.collect(Collectors.toList());

		//Remove dimension cols that were only added to build the Dim Source definition that is joined to the SQ
		matchedColumnsSIL = matchedColumnsSIL.stream()//
				.filter(col -> !col.getColumnType().equals("Attribute"))//
				.collect(Collectors.toList());

		allTargetColumns = matchedColumnsSIL.stream()//
				.filter(col -> col.getTargetColumnFlag())//
				.collect(Collectors.toList());
		
		allTargetColumns.addAll(silWorkflow.getColumns().stream()//
				.filter(col -> col.isTargetColumn() && col.getColumnType().equals("Foreign Key"))//
				.map(col -> {

					return SILInfaSourceColumnDefinition.builder()//
							.autoColumnFlag(false).domainLookupColumnFlag(false).legacyColumnFlag(false)
							.targetColumnFlag(true).miniDimColumnFlag(false).columnType("").columnDataType("NUMBER")//
							.columnLength(10)//
							.columnName(col.getColumnName())//
							.columnNumber(col.getColumnOrder())//
							.nullable("NOTNULL")//
							.offset(0)//
							.physicalLength(10)//
							.physicalOffset(0)//
							.precision(10)//
							.scale(0)//
							.selected(true)//
							.build();
				}).collect(Collectors.toList()));

		sourceTable = DataSourceTableDTO.builder()//
				.sourceName(SOURCE_NAME_LAW)//
				.tableName(STG_TABLE_INVOICE_LN)//
				.tableOwner(TABLE_OWNER_LAW)//
				.build();

	}

	private InfaMappingObject generateMapping() throws Exception {

		mappingService = new SILFactMappingGenerator(silWorkflow, //
				allSourceColumns, //
				allOneToOneDimColumns, //
				allTargetColumns, //
				matchedColumnsSIL, //
				sourceSystem, //
				sourceTable, //
				marshaller, //
				sourcetoXformDataTypeMapper, //
				sourceToTargetDatatypeMapper);

		return mappingService.getMapping();
	}

	@Test
	public void generatesCorrectSourceDefinitonForStagingTable() throws Exception {

		Optional<SOURCE> optSource = generateMapping()//
				.getFolderObjects()//
				.stream()//
				.filter(fo -> fo.getType().equals("SOURCE")).map(fo -> {
					return ((SOURCE) fo.getFolderObj());
				})//
				.filter(source -> source.getNAME().equals(STG_TABLE_INVOICE_LN)).findFirst();

		assertThat(optSource.get().getNAME()).isEqualTo(STG_TABLE_INVOICE_LN);

	}

	@Test
	public void generatesCorrectSourceDefinitonForDimensionSourceTable() throws Exception {

		Optional<SOURCE> optSource = generateMapping()//
				.getFolderObjects()//
				.stream()//
				.filter(fo -> fo.getType().equals("SOURCE")).map(fo -> {
					return ((SOURCE) fo.getFolderObj());
				})//
				.filter(source -> source.getNAME().equals("D_" + TABLE_INVOICE_LN)).findFirst();

		assertThat(optSource.get().getNAME()).isEqualTo("D_" + TABLE_INVOICE_LN);

	}

	@Test
	public void generatesCorrectTargetDefiniton() throws Exception {

		Optional<TARGET> optObject = generateMapping()//
				.getFolderObjects().stream().filter(fo -> fo.getType().equals("TARGET"))//
				.map(fo -> {
					return ((TARGET) fo.getFolderObj());
				})//
				.filter(target -> target.getNAME().equals("F_" + TABLE_INVOICE_LN))//
				.findFirst();

		assertThat(optObject.get().getNAME()).isEqualTo("F_" + TABLE_INVOICE_LN);
		assertThat(optObject.get().getTARGETFIELD().size()).isEqualTo(5);

	}

	@Test
	public void generatesSourceQualifierWithExpectedNameAndFieldCount() throws Exception {

		Optional<TRANSFORMATION> optObject = generateMapping()//
				.getMapping()//
				.getTRANSFORMATION().stream()
				.filter(xform -> xform.getTYPE().equals("Source Qualifier") && xform.getNAME().equals("SQ_ExtractData"))
				.findFirst();

		optObject.get().getTRANSFORMFIELD().stream().forEach(col -> log.info(":::::::::::::::" + col.getNAME()));

		assertThat(optObject.get().getNAME()).isEqualTo("SQ_ExtractData");
		assertThat(optObject.get().getTRANSFORMFIELD().size()).isEqualTo(5);

	}

	@Test
	public void generatesSilMappingWithCorrectAssociatedSourceInstance() throws Exception {

		// assert if the troublesome Associated instance is correctly set
		Optional<INSTANCE> optInstance = generateMapping()//
				.getMapping()//
				.getINSTANCE()//
				.stream()//
				.filter(instance -> instance.getNAME().equals("SQ_ExtractData"))//
				.findFirst();

		assertThat(optInstance.get().getASSOCIATEDSOURCEINSTANCE().get(0).getNAME()).isEqualTo("X_INVOICE_LN");

	}

	@Test
	public void generatesMappingWithCorrectNameAndInfaObjectCounts() throws Exception {

		InfaMappingObject mappingObj = generateMapping();

		assertThat(mappingObj.getMapping().getNAME()).isEqualTo("SIL_" + TABLE_INVOICE_LN + "_Dimension");
		// assertThat(mappingObj.getMapping().getTRANSFORMATION().size()).isEqualTo(9);
		// assertThat(mappingObj.getMapping().getINSTANCE().size()).isEqualTo(10);
		// assertThat(mappingObj.getMapping().getCONNECTOR().size()).isEqualTo(36);
		// assertThat(mappingObj.getMapping().getTARGETLOADORDER().size()).isEqualTo(1);
		// assertThat(mappingObj.getMapping().getMAPPINGVARIABLE().size()).isEqualTo(3);

	}

	@Test
	public void generatesMappingWithUnionXformToMergeUnspecAndInput() throws Exception {

		Optional<TRANSFORMATION> optObject = generateMapping()//
				.getMapping()//
				.getTRANSFORMATION().stream().filter(xform -> xform.getTYPE().equals("Custom Transformation")
						&& xform.getNAME().equals("UNION_UnspecifiedData"))
				.findFirst();

		assertThat(optObject.get().getNAME()).isEqualTo("UNION_UnspecifiedData");
		assertThat(optObject.get().getTEMPLATENAME()).isEqualTo("Union Transformation");
		// 42 = 14 x 3
		// 14 = 12 seeded cols plus 2 non-sys input cols
		assertThat(optObject.get().getTRANSFORMFIELD().size()).isEqualTo(42);

		// 14 x 2 = 28
		assertThat(optObject.get().getFIELDDEPENDENCY().size()).isEqualTo(28);

	}

	@Test
	public void generatesMappingWithSourcesConnectedToSourceQualifiers() throws Exception {
		List<CONNECTOR> connectors = generateMapping()//
				.getMapping()//
				.getCONNECTOR()//
				.stream()//
				.filter(conn -> (conn.getFROMINSTANCE().equals(STG_TABLE_INVOICE_LN)
						&& conn.getTOINSTANCE().equals("SQ_ExtractData")))
				.collect(Collectors.toList());

		assertThat(connectors.size()).isEqualTo(6);

	}

	@Test
	public void generatesMappingWithSQFieldsConnectedToUnionTransformFields() throws Exception {
		List<CONNECTOR> connectors = generateMapping()//
				.getMapping()//
				.getCONNECTOR()//
				.stream()//
				.filter(conn -> (conn.getFROMINSTANCE().equals("SQ_ExtractData")
						&& conn.getTOINSTANCE().equals("UNION_UnspecifiedData")))
				.collect(Collectors.toList());

		assertThat(connectors.size()).isEqualTo(6);

	}

	@Test
	public void generatesMappingWithCorrectHashExpression() throws Exception {

		Optional<TRANSFORMFIELD> optObject = generateMapping()//
				.getMapping()//
				.getTRANSFORMATION().stream()
				.filter(xform -> xform.getTYPE().equals("Expression") && xform.getNAME().equals("EXP_CalculateHashes"))
				.flatMap(expXform -> expXform.getTRANSFORMFIELD().stream())
				.filter(field -> field.getNAME().equals("HASH_RECORD")).findFirst();

		assertThat(optObject.get().getEXPRESSION()).isEqualTo(
				"MD5(IIF(ISNULL(INTEGRATION_ID),'NOVAL',INTEGRATION_ID)||IIF(ISNULL(PGUID),'NOVAL',PGUID)||IIF(ISNULL(ORIG_INV_NUM),'NOVAL',ORIG_INV_NUM)||IIF(ISNULL(SRC_BILL_EVT),'NOVAL',SRC_BILL_EVT))");

	}

	@Test
	public void generatesMappingWithUnionXformConnectsToHashExpression() throws Exception {
		List<CONNECTOR> connectors = generateMapping()//
				.getMapping()//
				.getCONNECTOR()//
				.stream()//
				.filter(conn -> (conn.getFROMINSTANCE().equals("UNION_UnspecifiedData")
						&& conn.getTOINSTANCE().equals("EXP_CalculateHashes")))
				.collect(Collectors.toList());
		// non system input columns
		assertThat(connectors.size()).isEqualTo(4);

	}

	@Test
	public void generatesMappingWithReusableXformForPguidLookup() throws Exception {

		Optional<TRANSFORMATION> optSource = generateMapping()//
				.getFolderObjects().stream().filter(fo -> fo.getType().equals("TRANSFORMATION"))//
				.map(fo -> {
					return ((TRANSFORMATION) fo.getFolderObj());
				}).filter(source -> source.getNAME().equals("LKP_SYS_Dimension_PGUID"))//
				.findFirst();

		assertThat(optSource.get().getNAME()).isEqualTo("LKP_SYS_Dimension_PGUID");
		assertThat(optSource.get().getTRANSFORMFIELD().size()).isEqualTo(7);

	}

	@Test
	public void generatesMappingWithFilterXform() throws Exception {

		Optional<TRANSFORMATION> optObject = generateMapping()//
				.getMapping()//
				.getTRANSFORMATION().stream()
				.filter(xform -> xform.getTYPE().equals("Filter") && xform.getNAME().equals("FIL_ExcludeRecords"))
				.findFirst();

		assertThat(optObject.get().getTRANSFORMFIELD().size()).isEqualTo(21);

	}

	@Test
	public void generatesMappingWithExpectedConnectorsIntoFilterXform() throws Exception {

		List<CONNECTOR> connectors = generateMapping()//
				.getMapping()//
				.getCONNECTOR()//
				.stream()//
				.filter(conn -> conn.getTOINSTANCE().equals("FIL_ExcludeRecords")).collect(Collectors.toList());

		// non system input columns
		assertThat(connectors.size()).isEqualTo(18);

	}

	@Test
	public void generatesMappingWithExpectedConnectorsIntoExpCollect() throws Exception {

		List<CONNECTOR> connectors = generateMapping()//
				.getMapping()//
				.getCONNECTOR()//
				.stream()//
				.filter(conn -> conn.getTOINSTANCE().equals("EXP_Collect")
						&& conn.getFROMINSTANCE().equals("MPL_Resolve_ChangeCapture"))
				.collect(Collectors.toList());

		// non system input columns
		assertThat(connectors.size()).isEqualTo(6);

	}

	@Test
	public void generatesMappingWithExpectedConnectorsIntoMappletCC() throws Exception {

		List<CONNECTOR> connectors = generateMapping()//
				.getMapping()//
				.getCONNECTOR()//
				.stream()//
				.filter(conn -> conn.getTOINSTANCE().equals("MPL_Resolve_ChangeCapture")
						&& conn.getFROMINSTANCE().equals("FIL_ExcludeRecords"))
				.collect(Collectors.toList());

		// non system input columns
		assertThat(connectors.size()).isEqualTo(6);

	}

}
