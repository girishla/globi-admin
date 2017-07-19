package com.globi.infa.generator;

import static com.globi.infa.generator.StaticObjectMother.targetDateField;
import static com.globi.infa.generator.StaticObjectMother.targetNumberField;
import static com.globi.infa.generator.StaticObjectMother.targetVarcharField;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.globi.AbstractIntegrationTest;
import com.globi.infa.datasource.fbm.FBMTableColumnRepository;
import com.globi.infa.datasource.type.oracle.OracleInfaSourceToInfaTargetTypeMapper;
import com.globi.infa.datasource.type.oracle.OracleTableColumnMetadataVisitor;
import com.globi.infa.generator.builder.InfaTargetObject;
import com.globi.infa.generator.builder.TargetDefinitionBuilder;
import com.globi.infa.metadata.pdl.InfaPuddleDefinitionRepositoryWriter;
import com.globi.infa.workflow.PTPWorkflow;




public class TargetDefinitionRepositoryWriterTest extends AbstractIntegrationTest  {

	List<InfaTargetObject> tgtObjects=new ArrayList<>();
	
	@Autowired
	InfaPuddleDefinitionRepositoryWriter tgtRepoWriter;
	
	@Autowired
	OracleInfaSourceToInfaTargetTypeMapper mapper;
	
	@Autowired
	private FBMTableColumnRepository colRepo;
	
	@Autowired
	private OracleTableColumnMetadataVisitor queryVisitor; 
	
	private PTPGeneratorInputBuilder inputBuilder;
	
	@Before
	public void setup(){
		
		inputBuilder=new PTPGeneratorInputBuilder(colRepo,queryVisitor);
		String targetname="FBM_PS_LN_BI_INV_HD_VW";
		
		InfaTargetObject tgtObj=new InfaTargetObject(TargetDefinitionBuilder.newBuilder().noMarshaller()//
				.mapper(mapper)				
				.addTargetField(targetNumberField("ROW_WID"))//
				.addTargetField(targetVarcharField("INTEGRATION_ID",100))//
				.addTargetField(targetDateField("SYS_EFF_FROM_DT"))//
				.noMoreFields()//
				.name(targetname)//
				.build());

		tgtObjects.add(tgtObj);
		
	}
	
	
	@Test @Ignore
	public void savesTargetTableAndColumnsIntoDatabaseWhenGivenATargetDefinitionObject(){
		
		tgtRepoWriter.writeToRepository(tgtObjects,inputBuilder.start()//
				.sourceName("FBM")//
				.tableName("PS_LN_BI_INV_HD_VW")//
				.setIntegrationCol("INVOICE")//
				.setBuidCol("BUSINESS_UNIT")//
				.setPguidCol("INVOICE")//
				.changeCaptureCol("LAST_UPDATE_DTTM")//
				.sourceFilter("PS_LN_BI_INV_HD_VW.BUSINESS_UNIT IN ('00AU0', '00NZ0', '00UK1')").build());
		
		
	}
	
	
}
