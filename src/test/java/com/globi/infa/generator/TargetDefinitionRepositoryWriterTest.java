package com.globi.infa.generator;

import static com.globi.infa.generator.StaticObjectMother.targetDateField;
import static com.globi.infa.generator.StaticObjectMother.targetNumberField;
import static com.globi.infa.generator.StaticObjectMother.targetVarcharField;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.globi.AbstractIntegrationTest;
import com.globi.infa.generator.builder.InfaTargetObject;
import com.globi.infa.generator.builder.TargetDefinitionBuilder;
import com.globi.infa.metadata.tgt.InfaTargetDefinitionRepository;
import com.globi.infa.metadata.tgt.InfaTargetDefinitionRepositoryWriter;




public class TargetDefinitionRepositoryWriterTest extends AbstractIntegrationTest  {

	List<InfaTargetObject> tgtObjects=new ArrayList<>();
	
	@Autowired
	InfaTargetDefinitionRepositoryWriter tgtRepoWriter;
	
	
	@Before
	public void setup(){
		
		
		String targetname="FBM_PS_LN_BI_INV_HD_VW";
		
		InfaTargetObject tgtObj=new InfaTargetObject(TargetDefinitionBuilder.newBuilder().noMarshaller()//
				.addTargetField(targetNumberField("ROW_WID"))//
				.addTargetField(targetVarcharField("INTEGRATION_ID",100))//
				.addTargetField(targetDateField("EFF_FROM_DT"))//
				.noMoreFields()//
				.name(targetname)//
				.build());

		tgtObjects.add(tgtObj);
		
	}
	
	
	@Test
	
	public void savesTargetTableAndColumnsIntoDatabaseWhenGivenATargetDefinitionObject(){
		
		tgtRepoWriter.writeToRepository(tgtObjects);
		
		
	}
	
	
}
