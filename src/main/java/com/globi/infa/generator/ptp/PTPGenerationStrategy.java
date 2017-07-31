package com.globi.infa.generator.ptp;

import static com.globi.infa.generator.builder.InfaObjectMother.getFolderFor;
import static com.globi.infa.generator.builder.InfaObjectMother.getRepository;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.springframework.context.annotation.Scope;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.globi.infa.datasource.core.SourceMetadataFactoryMapper;
import com.globi.infa.generator.AbstractGenerationStrategy;
import com.globi.infa.generator.InfaGenerationStrategy;
import com.globi.infa.generator.WorkflowGenerationException;
import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.generator.builder.PowermartObjectBuilder;
import com.globi.infa.generator.builder.WorkflowDefinitionBuilder;
import com.globi.infa.metadata.core.SourceTableAbbreviationMap;
import com.globi.infa.workflow.GeneratedWorkflow;
import com.globi.infa.workflow.InfaWorkflow;
import com.globi.infa.workflow.PTPWorkflow;


@Service
@Scope("prototype")
public class PTPGenerationStrategy extends AbstractGenerationStrategy implements InfaGenerationStrategy {

	private SourceTableAbbreviationMap sourceTableAbbreviation;

	PTPGenerationStrategy(Jaxb2Marshaller marshaller,//
			SourceMetadataFactoryMapper metadataFactoryMapper,//
			SourceTableAbbreviationMap sourceTableAbbreviation) {

		super(marshaller, metadataFactoryMapper);
		this.sourceTableAbbreviation = sourceTableAbbreviation;

	}

	private InfaPowermartObject generateWorkflow(PTPWorkflow wfDefinition) throws IOException, SAXException, JAXBException {


		this.setContext(wfDefinition.getSourceName(),wfDefinition);
		
		String tblName = wfDefinition.getSourceTableName();
		String dbName = wfDefinition.getSourceName();

		String targetTableName = wfDefinition.getTargetTableName();
		String targetTableDefnName = targetTableName.isEmpty() ? dbName + "_" + tblName : targetTableName;
		
		PTPPrimaryMappingGenerator primaryMappingGenerator=new PTPPrimaryMappingGenerator(generatorContext,marshaller);
		PTPExtractMappingGenerator extractMappingGenerator=new PTPExtractMappingGenerator(generatorContext,marshaller,sourceTableAbbreviation);

		InfaPowermartObject pmObj = PowermartObjectBuilder//
				.newBuilder()//
				.powermartObject().repository(getRepository())//
				.folder(getFolderFor("LAW_PTP_" + dbName, "Pull to puddle folder"))//
				.marshaller(marshaller)//
				.mappingDefn(extractMappingGenerator.getExtractMapping())//
				.mappingDefn(primaryMappingGenerator.getPrimaryMapping())//
				.noMoreMappings()//
				.setdefaultConfigFromSeed("Seed_DefaultSessionConfig")//
				.workflow(WorkflowDefinitionBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setValue("phasePrefix", "PTP")//
						.setValue("sourceShortCode", dbName)//
						.setValue("TargetShortCode", "PDL")//
						.setValue("tableName", tblName)//
						.setValue("tgtTableName", targetTableDefnName)//
						.noMoreValues()//
						.loadWorkflowFromSeed("Seed_WFPTP")//
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