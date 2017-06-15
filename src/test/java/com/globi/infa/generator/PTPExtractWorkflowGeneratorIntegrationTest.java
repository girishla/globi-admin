package com.globi.infa.generator;

import static com.globi.infa.generator.StaticObjectMother.getBuidColumn;
import static com.globi.infa.generator.StaticObjectMother.getCCColumn;
import static com.globi.infa.generator.StaticObjectMother.getIntegrationIdColumn;
import static com.globi.infa.generator.StaticObjectMother.getNormalColumn;
import static org.junit.Assert.assertThat;
import static org.xmlunit.matchers.HasXPathMatcher.hasXPath;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.annotation.Rollback;

import com.globi.AbstractIntegrationTest;
import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.metadata.pdl.InfaPuddleDefinitionRepositoryWriter;
import com.globi.infa.workflow.InfaWorkflow;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowRepository;

public class PTPExtractWorkflowGeneratorIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private Jaxb2Marshaller marshaller;

	@Autowired
	private PTPExtractGenerationStrategy generator;

	@Autowired
	PTPWorkflowRepository ptpRepository;

	@Autowired
	FileWriterEventListener fileWriter;

	@Autowired
	PTPRepositoryWriterEventListener repoWriter;

	@Autowired
	GitWriterEventListener gitWriter;

	@Autowired
	private PTPPrimaryGenerationStrategy ptpPrimarygenerator;

	@Autowired
	private InfaPuddleDefinitionRepositoryWriter targetDefnWriter;

	private PTPWorkflow ptpWorkflowInputToGenerator;

	@Before
	public void setup() {

		String sourceTable = "S_ORG_EXT";
		String source = "CUK";

		ptpWorkflowInputToGenerator = PTPWorkflow.builder()//
				.sourceName(source)//
				.sourceTableName(sourceTable)//
				.column(getIntegrationIdColumn("ROW_ID"))//
				.column(getCCColumn("LAST_UPD"))
				.column(getNormalColumn("NAME"))//
				.column(getBuidColumn("BU_ID"))
				.workflowUri("/GeneratedWorkflows/Repl/" + "PTP_" + source+ "_"+ sourceTable  + ".xml")
				.workflowType("PTP")
				.workflowName("PTP_" + source+ "_"+ sourceTable  + "_Extract")
				.build();
				

	}

	private void assertContentOk(InfaPowermartObject pmObj) throws Exception {

		String testXML = asString(marshaller.getJaxbContext(), pmObj.pmObject);

		assertThat(testXML, (hasXPath("/POWERMART/REPOSITORY/FOLDER/SOURCE")));
		assertThat(testXML, (hasXPath("/POWERMART/REPOSITORY/FOLDER/TARGET")));
		assertThat(testXML, (hasXPath("/POWERMART/REPOSITORY/FOLDER/MAPPING")));
		assertThat(testXML, (hasXPath("/POWERMART/REPOSITORY/FOLDER/CONFIG")));
		assertThat(testXML, (hasXPath("/POWERMART/REPOSITORY/FOLDER/WORKFLOW")));
		assertThat(testXML, (hasXPath("/POWERMART/REPOSITORY/FOLDER/MAPPLET")));

		assertThat(testXML, (hasXPath(
				"/POWERMART/REPOSITORY/FOLDER/MAPPING/INSTANCE[@NAME='SQ_ExtractData']/ASSOCIATED_SOURCE_INSTANCE")));
	}
	
	@Test
	@Rollback(false)
	public void generatesPTPWorkflowForOrgExtTable()
			throws Exception {

		generator.setWfDefinition(ptpWorkflowInputToGenerator);
		generator.addListener(targetDefnWriter);
		generator.addListener(gitWriter);

		InfaPowermartObject pmObj = generator.generate();

		ptpPrimarygenerator.setWfDefinition(ptpWorkflowInputToGenerator);
		ptpPrimarygenerator.addListener(targetDefnWriter);
		ptpPrimarygenerator.addListener(gitWriter);

		ptpPrimarygenerator.generate();

		Optional<PTPWorkflow> existingWorkflow = ptpRepository
				.findByWorkflowName(ptpWorkflowInputToGenerator.getWorkflow().getWorkflowName());
		if (existingWorkflow.isPresent()) {
			ptpRepository.delete(existingWorkflow.get());
		}

		ptpRepository.save(ptpWorkflowInputToGenerator);

		assertContentOk(pmObj);

	}

}
