package com.globi.infa.generator;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import com.globi.AbstractIntegrationTest;
import com.globi.infa.datasource.type.sqlserver.SQLServerTableColumnMetadataVisitor;
import com.globi.infa.datasource.vpt.VPTTableColumnRepository;
import com.globi.infa.metadata.pdl.InfaPuddleDefinitionRepositoryWriter;
import com.globi.infa.workflow.InfaPTPWorkflowRepository;
import com.globi.infa.workflow.PTPWorkflow;

public class PTPGeneratorDDLGenerationTest extends AbstractIntegrationTest {

	@Autowired
	InfaPTPWorkflowRepository wfRepository;
	
	@Autowired
	private VPTTableColumnRepository colRepo;
	
	@Autowired
	private SQLServerTableColumnMetadataVisitor queryVisitor; 
	
	
	PTPWorkflow ptpWorkflow;

	private PTPGeneratorInputBuilder inputBuilder;

	@Autowired
	private InfaPuddleDefinitionRepositoryWriter targetDefnWriter;

	@Autowired
	private PTPExtractGenerationStrategy generator;

	@Before
	public void setup() {


		inputBuilder= new PTPGeneratorInputBuilder(colRepo,queryVisitor);
		
		ptpWorkflow = inputBuilder.start()//
				.sourceName("VPT")//
				.tableName("YearOnYearSummary")//
				.setIntegrationCol("CustomerID")//
				.setNormalCol("CustomerName")
				.setNormalCol("NetValue_PYE")
				.setNormalCol("CY v PY NetValue")
				.setNormalCol("#Accounts_PY")
				.sourceFilter("")
				.build();
				

	}


	
	@Test
	@Rollback(false)
	@Ignore //as it doesnt assert anything
	public void generatesDDLForOrgExtTable()
			throws Exception {

		generator.setWfDefinition(ptpWorkflow);
		generator.addListener(targetDefnWriter);

		generator.generate();



	}

}
