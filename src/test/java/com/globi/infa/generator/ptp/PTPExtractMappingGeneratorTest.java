package com.globi.infa.generator.ptp;

import static com.globi.infa.generator.StaticObjectMother.getCCColumn;
import static com.globi.infa.generator.StaticObjectMother.getInfaSourceColumnsFromSourceDefn;
import static com.globi.infa.generator.StaticObjectMother.getIntegrationIdColumn;
import static com.globi.infa.generator.StaticObjectMother.getNormalColumn;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

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
import com.globi.infa.metadata.core.StringMap;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowSourceColumn;
import com.globi.metadata.sourcesystem.SourceSystem;

public class PTPExtractMappingGeneratorTest {

	private DataTypeMapper dataTypeMapper;
	private DataTypeMapper sourceToTargetDatatypeMapper;
	private SourceSystem sourceSystem;
	private DataSourceTableDTO sourceTable;
	private Jaxb2Marshaller marshaller;
	private PTPWorkflow ptpWorkflow;
	private List<InfaSourceColumnDefinition> allSourceColumns;

	private static final String SOURCE_TABLE_SBU = "S_BU";
	private static final String SOURCE_NAME_CGL = "CGL";
	private static final String TABLE_OWNER_SIEBEL = "SIEBEL";
	private static final String DB_TYPE_ORACLE = "Oracle";
	private static final int SOURCE_NUM_CGL = 1;

	private PTPExtractMappingGenerator mappingService;
	private StringMap strMap;

	@Before
	public void setUp() throws Exception {

		dataTypeMapper = new OracleInfaSourceToInfaXFormTypeMapper();
		sourceToTargetDatatypeMapper = new OracleInfaSourceToInfaTargetTypeMapper();
		sourceSystem = SourceSystem.builder()//
				.dbName(SOURCE_NAME_CGL)//
				.dbType(DB_TYPE_ORACLE)//
				.name(SOURCE_NAME_CGL)//
				.ownerName(TABLE_OWNER_SIEBEL)//
				.sourceNum(SOURCE_NUM_CGL)//
				.build();

		marshaller = new InfaConfig().jaxb2Marshaller();

		List<PTPWorkflowSourceColumn> cols = new ArrayList<>();
		cols.add(getIntegrationIdColumn("ROW_ID"));
		cols.add(getNormalColumn("ROW_ID"));
		cols.add(getCCColumn("LAST_UPD"));

		ptpWorkflow = PTPWorkflow.builder()//
				.sourceName(SOURCE_NAME_CGL)//
				.columns(cols)//
				.sourceTableName(SOURCE_TABLE_SBU)//
				.workflowUri("")
				.workflowName("PTP_" + SOURCE_NAME_CGL + "_" + SOURCE_TABLE_SBU)//
				.targetTableName(SOURCE_NAME_CGL + "_" + SOURCE_TABLE_SBU).build();

		allSourceColumns = getInfaSourceColumnsFromSourceDefn(SourceDefinitionBuilder//
				.newBuilder()//
				.sourceFromSeed("seedClass")//
				.marshaller(marshaller)//
				.loadSourceFromSeed("TEST_SEED_SOURCE_SBU")//
				.noFields()//
				.name(SOURCE_TABLE_SBU)//
				.build()//
		);

		sourceTable = DataSourceTableDTO.builder()//
				.sourceName(SOURCE_NAME_CGL)//
				.tableName(SOURCE_TABLE_SBU)//
				.tableOwner(TABLE_OWNER_SIEBEL)//
				.build();

		strMap = mock(StringMap.class);
		String tableUniqueName = sourceSystem.getName() + "_" + SOURCE_TABLE_SBU;
		when(strMap.map(tableUniqueName)).thenReturn("BUN");


	}
	
	
	@Test
	public void generatesPtpExtractMappingForSourceTable() throws Exception{
		
		mappingService = new PTPExtractMappingGenerator(ptpWorkflow, //
				allSourceColumns, //
				sourceSystem, //
				sourceTable, //
				strMap, //
				marshaller, //
				dataTypeMapper, //
				sourceToTargetDatatypeMapper);
		
		InfaMappingObject mappingObj= mappingService.getExtractMapping();
	
		assertThat(mappingObj.getMapping().getNAME()).isEqualTo("PTP_" + SOURCE_NAME_CGL + "_" + SOURCE_TABLE_SBU + "_Extract");
		
	
	}
	
	

}
