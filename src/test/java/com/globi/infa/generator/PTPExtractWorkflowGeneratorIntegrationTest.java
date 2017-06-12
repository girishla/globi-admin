package com.globi.infa.generator;

import static org.xmlunit.matchers.HasXPathMatcher.hasXPath;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.annotation.Rollback;
import org.xml.sax.SAXException;

import com.globi.AbstractIntegrationTest;
import com.globi.infa.datasource.core.OracleTableColumnMetadataVisitor;
import com.globi.infa.datasource.fbm.FBMTableColumnRepository;
import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.workflow.InfaWorkflow;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowRepository;
import com.globi.infa.workflow.PTPWorkflowSourceColumn;
import com.rits.cloning.Cloner;
import static com.globi.infa.generator.StaticObjectMother.*;

import xjc.POWERMART;

public class PTPExtractWorkflowGeneratorIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private Jaxb2Marshaller marshaller;

	@Autowired
	private PTPExtractGenerationStrategy generator;

	@Autowired
	PTPWorkflowRepository wfRepository;


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

		ptpWorkflowInputToGenerator  = PTPWorkflow.builder()//
				.sourceName(source)//
				.sourceTableName(sourceTable)//
				.column(getIntegrationIdColumn("ROW_ID"))
				.column(getIntegrationIdColumn("LAST_UPD"))
				.column(getNormalColumn("NAME"))
				.column(getBuidColumn("BU_ID"))
				.workflow(InfaWorkflow.builder()//
						.workflowUri("/GeneratedWorkflows/Repl/" + "PTP_" + sourceTable + ".xml")//
						.workflowName("PTP_" + sourceTable + "_Extract")//
						.workflowType("PTP")//
						.build())
				.build();

	}



	@Test
	@Rollback(false)
	public void generatesPTPWorkflowForOrgExtTable()
			throws JAXBException, FileNotFoundException, IOException, SAXException {



		generator.setWfDefinition(ptpWorkflowInputToGenerator);
		generator.addListener(fileWriter);
		generator.addListener(gitWriter);
//		generator.addListener(repoWriter);

		InfaPowermartObject pmObj = generator.generate();
		
		String testXML = asString(marshaller.getJaxbContext(), pmObj.pmObject);

		assertThat(testXML, (hasXPath("/POWERMART/REPOSITORY/FOLDER/SOURCE")));
		assertThat(testXML, (hasXPath("/POWERMART/REPOSITORY/FOLDER/TARGET")));
		assertThat(testXML, (hasXPath("/POWERMART/REPOSITORY/FOLDER/MAPPING")));
		assertThat(testXML, (hasXPath("/POWERMART/REPOSITORY/FOLDER/CONFIG")));
		assertThat(testXML, (hasXPath("/POWERMART/REPOSITORY/FOLDER/WORKFLOW")));

		assertThat(testXML, (hasXPath(
				"/POWERMART/REPOSITORY/FOLDER/MAPPING/INSTANCE[@NAME='SQ_ExtractData']/ASSOCIATED_SOURCE_INSTANCE")));



	}




}
