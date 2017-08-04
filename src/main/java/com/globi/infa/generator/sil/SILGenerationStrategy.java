package com.globi.infa.generator.sil;

import static com.globi.infa.generator.builder.InfaObjectMother.getFolderFor;
import static com.globi.infa.generator.builder.InfaObjectMother.getRepository;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.springframework.context.annotation.Scope;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.globi.infa.datasource.core.MetadataFactoryMapper;
import com.globi.infa.generator.AbstractGenerationStrategy;
import com.globi.infa.generator.InfaGenerationStrategy;
import com.globi.infa.generator.WorkflowGenerationException;
import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.generator.builder.PowermartObjectBuilder;
import com.globi.infa.generator.builder.WorkflowDefinitionBuilder;
import com.globi.infa.generator.ptp.PTPGeneratorContext;
import com.globi.infa.metadata.core.StringMap;
import com.globi.infa.workflow.GeneratedWorkflow;
import com.globi.infa.workflow.InfaWorkflow;
import com.globi.infa.workflow.SILWorkflow;



@Service
@Scope("prototype")
public class SILGenerationStrategy extends AbstractGenerationStrategy implements InfaGenerationStrategy {

	private SILGeneratorContext generatorContext;

	SILGenerationStrategy(Jaxb2Marshaller marshaller,//
			MetadataFactoryMapper metadataFactoryMapper,//
			StringMap sourceTableAbbreviation) {

		super(marshaller, metadataFactoryMapper);

	}
	
	
	private void setContext(String sourceName,String tblName,InfaWorkflow inputWF) {
		
		generatorContext=SILGeneratorContext.contextFor(sourceName,tblName, metadataFactoryMapper,inputWF);

	}
	

	private InfaPowermartObject generateWorkflow(SILWorkflow wfDefinition) throws IOException, SAXException, JAXBException {



//		
//		String tblName = wfDefinition.getSourceTableName();
//		String dbName = wfDefinition.getSourceName();
//		String targetTableName = wfDefinition.getTargetTableName();
//		String targetTableDefnName = targetTableName.isEmpty() ? dbName + "_" + tblName : targetTableName;

		
		this.setContext("LAW",wfDefinition.getStageName(),wfDefinition);
		
//		SILPrimaryMappingGenerator primaryMappingGenerator=new SILPrimaryMappingGenerator(//
//				(SILWorkflow) generatorContext.getInputWF()//
//				,generatorContext.getAllSourceColumns(),//
//				generatorContext.getMatchedColumnsSIL(),//
//				generatorContext.getSource(),//
//				generatorContext.getSourceTable(),//
//				marshaller,//
//				generatorContext.getDataTypeMapper(),//
//				generatorContext.getSourceToTargetDatatypeMapper());
//	
//		SILExtractMappingGenerator extractMappingGenerator=new SILExtractMappingGenerator(//
//				(SILWorkflow) generatorContext.getInputWF()//
//				,generatorContext.getAllSourceColumns(),//
//				generatorContext.getMatchedColumnsSIL(),//
//				generatorContext.getSource(),//
//				generatorContext.getSourceTable(),//
//				sourceTableAbbreviation,//
//				marshaller,//
//				generatorContext.getDataTypeMapper(),//
//				generatorContext.getSourceToTargetDatatypeMapper());

		InfaPowermartObject pmObj = PowermartObjectBuilder//
				.newBuilder()//
				.powermartObject().repository(getRepository())//
				.folder(getFolderFor("LAW_SIL_AUTO" , "SIL folder to hold autogen objects"))//
				.marshaller(marshaller)//
//				.mappingDefn(extractMappingGenerator.getExtractMapping())//
//				.mappingDefn(primaryMappingGenerator.getPrimaryMapping())//
				.noMoreMappings()//
				.setdefaultConfigFromSeed("Seed_SIL_DefaultSessionConfig")//
				.workflow(WorkflowDefinitionBuilder.newBuilder()//
						.marshaller(marshaller)//
						.noMoreValues()//
						.loadWorkflowFromSeed("Seed_SIL_WF")//
						.nameAlreadySet()//
						.build())//
				.build();

		pmObj.pmObjectName = "SIL_Test_Dimension";

		return pmObj;
	}

	@Override
	public InfaPowermartObject generate(InfaWorkflow inputWF) {
		InfaPowermartObject pmObj = null;
		SILWorkflow wfDefinition=(SILWorkflow) inputWF;
		
		try {
			pmObj = this.generateWorkflow(wfDefinition);
			this.notifyListeners(pmObj, wfDefinition);
		} catch (Exception e) {
			e.printStackTrace();

			throw new WorkflowGenerationException((GeneratedWorkflow) wfDefinition, e.getMessage());

		}

		return pmObj;

	}


}