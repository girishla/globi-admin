package com.globi.infa.generator.sil;

import static com.globi.infa.generator.CommonStaticObjectMother.getInfaSourceColumnsFromSourceDefn;
import static com.globi.infa.generator.sil.SILStaticObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import com.globi.infa.workflow.SILWorkflow;
import com.globi.infa.workflow.SILWorkflowSourceColumn;
import com.globi.metadata.sourcesystem.SourceSystem;

import lombok.extern.slf4j.Slf4j;
import xjc.SOURCE;
import xjc.TRANSFORMATION;

@Slf4j
public class SILDimensionMappingGeneratorTest {

	private DataTypeMapper sourcetoXformDataTypeMapper;
	private DataTypeMapper sourceToTargetDatatypeMapper;
	private SourceSystem sourceSystem;
	private DataSourceTableDTO sourceTable;
	private Jaxb2Marshaller marshaller;
	private SILWorkflow silWorkflow;
	private List<InfaSourceColumnDefinition> allSourceColumns;

	private static final String STG_TABLE_INVOICE_LN = "X_INVOICE_LN";
	private static final String SOURCE_NAME_LAW = "LAW";
	private static final String TABLE_OWNER_LAW = "LAW";
	private static final String DB_TYPE_ORACLE = "Oracle";
	private static final int SOURCE_NUM_LAW = 1;

	private SILDimensionMappingGenerator mappingService;

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
		cols.add(getSpecialColumn("INTEGRATION_ID","Natural Key"));
		cols.add(getSpecialColumn("DATASOURCE_NUM_ID","System"));
		cols.add(getSpecialColumn("PGUID","Natural Key"));
		cols.add(getSpecialColumn("BU_PGUID","System"));
		cols.add(getDimensionAttribColumn("ORIG_INV_NUM"));
		cols.add(getDimensionAttribColumn("SRC_BILL_EVT"));

		
		
		
		
		
		silWorkflow = SILWorkflow.builder()//
				.loadType("Dimension").stageName(STG_TABLE_INVOICE_LN).columns(cols)//
				.workflowUri("")//
				.workflowName("SIL_" + STG_TABLE_INVOICE_LN + "_Dimension")//
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

		sourceTable = DataSourceTableDTO.builder()//
				.sourceName(SOURCE_NAME_LAW)//
				.tableName(STG_TABLE_INVOICE_LN)//
				.tableOwner(TABLE_OWNER_LAW)//
				.build();

	}

	private InfaMappingObject generateMapping() throws Exception {


		mappingService = new SILDimensionMappingGenerator(silWorkflow, //
				allSourceColumns, //
				SILGeneratorContext.getFilteredSourceDefnColumns(allSourceColumns, silWorkflow.getColumns()), //
				sourceSystem, //
				sourceTable, //
				marshaller, //
				sourcetoXformDataTypeMapper, //
				sourceToTargetDatatypeMapper);

		return mappingService.getMapping();
	}

	@Test
	public void generatesCorrectSourceDefinitonForUnspecifiedVirtualTable() throws Exception{
		
		Optional<SOURCE> optSource = generateMapping()//
				.getFolderObjects()
				.stream()
				.filter(fo->fo.getType().equals("SOURCE"))
				.map(fo->{
					return ((SOURCE)fo.getFolderObj());
				})
				.filter(source->source.getNAME().equals("VIRTUAL_EXP"))
				.findFirst();
				
		assertThat(optSource.get().getNAME()).isEqualTo("VIRTUAL_EXP");
		assertThat(optSource.get().getSOURCEFIELD().size()).isEqualTo(3);
		
		
	}
	
	
	
	
	
	@Test
	public void generatesCorrectSourceQualifierForVirtualUnspecified() throws Exception{
		
		
		Optional<TRANSFORMATION> optObject = generateMapping()//
				.getMapping()//
				.getTRANSFORMATION()
				.stream()
				.filter(xform->xform.getTYPE().equals("Source Qualifier") && xform.getNAME().equals("SQ_ExtractUnspecified"))
				.findFirst();
		
			assertThat(optObject.get().getNAME()).isEqualTo("SQ_ExtractUnspecified");
			assertThat(optObject.get().getTRANSFORMFIELD().size()).isEqualTo(5);
	}
	
	
	
	
	@Test
	public void generatesCorrectSourceDefinitonForStagingTable() throws Exception{
		
		Optional<SOURCE> optSource = generateMapping()//
				.getFolderObjects()
				.stream()
				.filter(fo->fo.getType().equals("SOURCE"))
				.map(fo->{
					return ((SOURCE)fo.getFolderObj());
				})
				.filter(source->source.getNAME().equals(STG_TABLE_INVOICE_LN))
				.findFirst();
				
		assertThat(optSource.get().getNAME()).isEqualTo(STG_TABLE_INVOICE_LN);
	
		
	}
	
	
	@Test
	public void generatesSourceQualifierWithExpectedNameAndFieldCountForStagingTable() throws Exception{
		
		
		Optional<TRANSFORMATION> optObject = generateMapping()//
				.getMapping()//
				.getTRANSFORMATION()
				.stream()
				.filter(xform->xform.getTYPE().equals("Source Qualifier") && xform.getNAME().equals("SQ_ExtractData"))
				.findFirst();
		
		assertThat(optObject.get().getNAME()).isEqualTo("SQ_ExtractData");
		assertThat(optObject.get().getTRANSFORMFIELD().size()).isEqualTo(6);	
			
			
	}
	
	
	
	

/*	@Test
	public void generatesSilMappingWithCorrectAssociatedSourceInstance() throws Exception {

		// assert if the troublesome Associated instance is correctly set
		Optional<INSTANCE> optInstance = generateMapping()//
				.getMapping()//
				.getINSTANCE()//
				.stream()//
				.filter(instance -> instance.getNAME().equals("SQ_ExtractData"))//
				.findFirst();
		assertThat(optInstance.get().getASSOCIATEDSOURCEINSTANCE().get(0).getNAME()).isEqualTo("S_BU");

	}
*/
	

	@Test
	public void generatesMappingWithCorrectNameAndInfaObjectCounts() throws Exception {

		InfaMappingObject mappingObj = generateMapping();

		assertThat(mappingObj.getMapping().getNAME()).isEqualTo("SIL_" + STG_TABLE_INVOICE_LN + "_Dimension");
//		assertThat(mappingObj.getMapping().getTRANSFORMATION().size()).isEqualTo(9);
//		assertThat(mappingObj.getMapping().getINSTANCE().size()).isEqualTo(10);
//		assertThat(mappingObj.getMapping().getCONNECTOR().size()).isEqualTo(36);
//		assertThat(mappingObj.getMapping().getTARGETLOADORDER().size()).isEqualTo(1);
//		assertThat(mappingObj.getMapping().getMAPPINGVARIABLE().size()).isEqualTo(3);

	}

	
	@Test
	public void generatesMappingWithUnionXformToMergeUnspecAndInput() throws Exception{
		
		Optional<TRANSFORMATION> optObject = generateMapping()//
				.getMapping()//
				.getTRANSFORMATION()
				.stream()
				.filter(xform->xform.getTYPE().equals("Custom Transformation") && xform.getNAME().equals("UNION_UnspecifiedData"))
				.findFirst();
		
		
		optObject.get().getTRANSFORMFIELD().stream().forEach(field->log.info(field.getNAME()));
		
		assertThat(optObject.get().getNAME()).isEqualTo("UNION_UnspecifiedData");
		assertThat(optObject.get().getTEMPLATENAME()).isEqualTo("Union Transformation");
		//42 = 14 x 3
		//14 = 12 seeded cols plus 2 non-sys input cols
		assertThat(optObject.get().getTRANSFORMFIELD().size()).isEqualTo(42);	
		
		
	}
	






}
