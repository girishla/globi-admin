package com.globi.infa.generator.ptp;

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
import com.globi.infa.metadata.core.StringMap;
import com.globi.infa.workflow.GeneratedWorkflow;
import com.globi.infa.workflow.InfaWorkflow;
import com.globi.infa.workflow.PTPWorkflow;


@Service
@Scope("prototype")
public class PTPGenerationStrategy extends AbstractGenerationStrategy implements InfaGenerationStrategy {

	private PTPGeneratorContext generatorContext;
	private StringMap sourceTableAbbreviation;

	PTPGenerationStrategy(Jaxb2Marshaller marshaller,//
			MetadataFactoryMapper metadataFactoryMapper,//
			StringMap sourceTableAbbreviation) {

		super(marshaller, metadataFactoryMapper);
		this.sourceTableAbbreviation = sourceTableAbbreviation;

	}
	
	
	private void setContext(String sourceName,String tblName,InfaWorkflow inputWF) {
		
		generatorContext=PTPGeneratorContext.contextFor(sourceName,tblName, metadataFactoryMapper,inputWF);

	}
	
	

	private InfaPowermartObject generateWorkflow(PTPWorkflow wfDefinition) throws IOException, SAXException, JAXBException {



		
		String tblName = wfDefinition.getSourceTableName();
		String dbName = wfDefinition.getSourceName();
		String targetTableName = wfDefinition.getTargetTableName();
		String targetTableDefnName = targetTableName.isEmpty() ? dbName + "_" + tblName : targetTableName;

		
		this.setContext(dbName,tblName,wfDefinition);
		
		PTPPrimaryMappingGenerator primaryMappingGenerator=new PTPPrimaryMappingGenerator(//
				(PTPWorkflow) generatorContext.getInputWF()//
				,generatorContext.getAllSourceColumns(),//
				generatorContext.getMatchedColumnsPTP(),//
				generatorContext.getSource(),//
				generatorContext.getSourceTable(),//
				marshaller,//
				generatorContext.getDataTypeMapper(),//
				generatorContext.getSourceToTargetDatatypeMapper());
	
		PTPExtractMappingGenerator extractMappingGenerator=new PTPExtractMappingGenerator(//
				(PTPWorkflow) generatorContext.getInputWF()//
				,generatorContext.getAllSourceColumns(),//
				generatorContext.getMatchedColumnsPTP(),//
				generatorContext.getSource(),//
				generatorContext.getSourceTable(),//
				sourceTableAbbreviation,//
				marshaller,//
				generatorContext.getDataTypeMapper(),//
				generatorContext.getSourceToTargetDatatypeMapper());

		InfaPowermartObject pmObj = PowermartObjectBuilder//
				.newBuilder()//
				.powermartObject().repository(getRepository())//
				.folder(getFolderFor("LAW_PTP_" + dbName, "Pull to puddle folder"))//
				.marshaller(marshaller)//
				.mappingDefn(extractMappingGenerator.getExtractMapping())//
				.mappingDefn(primaryMappingGenerator.getPrimaryMapping())//
				.noMoreMappings()//
				.setdefaultConfigFromSeed("Seed_PTP_DefaultSessionConfig")//
				.workflow(WorkflowDefinitionBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setValue("phasePrefix", "PTP")//
						.setValue("sourceShortCode", dbName)//
						.setValue("TargetShortCode", "PDL")//
						.setValue("tableName", tblName)//
						.setValue("tgtTableName", targetTableDefnName)//
						.noMoreValues()//
						.loadWorkflowFromSeed("Seed_PTP_WF")//
						.nameAlreadySet()//
						.build())//
				.build();

		pmObj.pmObjectName = "PTP_" + dbName + "_" + tblName;

		return pmObj;
	}

	@Override
	public InfaPowermartObject generate(InfaWorkflow inputWF) {
		InfaPowermartObject pmObj = null;
		PTPWorkflow wfDefinition=(PTPWorkflow) inputWF;
		
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