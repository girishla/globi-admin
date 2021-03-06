package com.globi.infa.generator.ptp;

import static com.globi.infa.generator.ptp.PTPStaticObjectMother.getBuidColumn;
import static com.globi.infa.generator.ptp.PTPStaticObjectMother.getCCColumn;
import static com.globi.infa.generator.ptp.PTPStaticObjectMother.getIntegrationIdColumn;
import static com.globi.infa.generator.ptp.PTPStaticObjectMother.getNormalColumn;
import static org.junit.Assert.assertThat;
import static org.xmlunit.matchers.HasXPathMatcher.hasXPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.annotation.Rollback;

import com.globi.AbstractIntegrationTest;
import com.globi.infa.generator.FileWriterEventListener;
import com.globi.infa.generator.GitWriterEventListener;
import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.workflow.ptp.InfaPTPWorkflowRepository;
import com.globi.infa.workflow.ptp.PTPWorkflow;
import com.globi.infa.workflow.ptp.PTPWorkflowSourceColumn;

public class PTPExtractWorkflowGeneratorIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private Jaxb2Marshaller marshaller;

	@Autowired
	private PTPGenerationStrategy generator;

	@Autowired
	InfaPTPWorkflowRepository ptpRepository;

	@Autowired
	FileWriterEventListener fileWriter;

	@Autowired
	PTPRepositoryWriterEventListener repoWriter;

	@Autowired
	GitWriterEventListener gitWriter;


	private PTPWorkflow ptpWorkflowInputToGenerator;

	@Before
	public void setup() {

		String sourceTable = "S_ORG_EXT";
		String source = "CUK";
		
		List<PTPWorkflowSourceColumn> cols=new ArrayList<>();
		cols.add(getIntegrationIdColumn("ROW_ID"));
		cols.add(getCCColumn("LAST_UPD"));
		cols.add(getNormalColumn("NAME"));
		cols.add(getBuidColumn("BU_ID"));
		
		ptpWorkflowInputToGenerator = PTPWorkflow.builder()//
				.sourceName(source)//
				.sourceTableName(sourceTable)//
				.columns(cols)//
				.workflowUri("/GeneratedWorkflows/Repl/" + "PTP_" + source+ "_"+ sourceTable  + ".xml")
				.workflowName("PTP_" + source+ "_"+ sourceTable  + "_Extract")
				.targetTableName(source+ "_"+ sourceTable)
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

		generator.addListener(gitWriter);

		InfaPowermartObject pmObj = generator.generate(ptpWorkflowInputToGenerator);


		Optional<PTPWorkflow> existingWorkflow = ptpRepository
				.findByWorkflowName(ptpWorkflowInputToGenerator.getWorkflow().getWorkflowName());
		if (existingWorkflow.isPresent()) {
			ptpRepository.delete(existingWorkflow.get());
		}

		ptpRepository.save(ptpWorkflowInputToGenerator);

		assertContentOk(pmObj);

	}

}
