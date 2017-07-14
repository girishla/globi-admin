package com.globi.infa.generator;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.globi.AbstractWebIntegrationTest;
import com.globi.infa.datasource.gcrm.GCRMTableColumnRepository;
import com.globi.infa.datasource.type.oracle.OracleTableColumnMetadataVisitor;
import com.globi.infa.datasource.type.sqlserver.SQLServerTableColumnMetadataVisitor;
import com.globi.infa.datasource.vpt.VPTTableColumnRepository;
import com.globi.infa.workflow.InfaPTPWorkflowRepository;
import com.globi.infa.workflow.PTPWorkflow;

public class PTPWorkflowVPTWebtest extends AbstractWebIntegrationTest {

	@Autowired
	InfaPTPWorkflowRepository wfRepository;
	
	@Autowired
	private VPTTableColumnRepository colRepo;
	
	@Autowired
	private SQLServerTableColumnMetadataVisitor queryVisitor; 
	
	
	PTPWorkflow ptpWorkflow;

	private PTPGeneratorInputBuilder inputBuilder;
	
	
	
	@Before
	public void setup(){
		
		
		inputBuilder= new PTPGeneratorInputBuilder(colRepo,queryVisitor);
		
		ptpWorkflow= inputBuilder.start()//
		.sourceName("VPT")//
		.tableName("YearOnYearSummary")//
		.setIntegrationCol("CustomerID")//
//		.setBuidCol("BU_ID")//
		.setNormalCol("CustomerName")
		.setNormalCol("NetValue_PYE")
		.sourceFilter("")
		.build();
		
	}


	@Test
	@WithMockUser
	public void createsWorkflowResourceFromWorkflowDefinition() throws Exception {

		mvc.perform(post("/infagen/workflows/ptp?sync=true")//
				.content(asJsonString(ptpWorkflow))//
				.contentType(MediaType.APPLICATION_JSON_VALUE)//
				.accept(MediaType.APPLICATION_JSON_VALUE))//
				.andDo(MockMvcResultHandlers.print())//
				.andExpect(status().isCreated())//
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))//
				.andExpect(jsonPath("$.workflowName", notNullValue()))//
				.andExpect(jsonPath("$.workflowUri", notNullValue()));

	}


}