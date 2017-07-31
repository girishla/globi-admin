package com.globi.infa.generator;

import static org.hamcrest.CoreMatchers.is;
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
import com.globi.infa.workflow.InfaPTPWorkflowRepository;
import com.globi.infa.workflow.PTPWorkflow;

public class PTPWorkflowWebtest extends AbstractWebIntegrationTest {

	@Autowired
	InfaPTPWorkflowRepository wfRepository;
	
	@Autowired
	private GCRMTableColumnRepository colRepo;
	
	@Autowired
	private OracleTableColumnMetadataVisitor queryVisitor; 
	
	
	PTPWorkflow ptpWorkflow;

	private PTPGeneratorInputBuilder inputBuilder;
	
	
	
	@Before
	public void setup(){
		
		
		inputBuilder= new PTPGeneratorInputBuilder(colRepo,queryVisitor);
		
		ptpWorkflow= inputBuilder.start()//
		.sourceName("CGL")//
		.tableName("S_BU")//
		.setIntegrationCol("ROW_ID")//
		.setChangeCaptureCol("LAST_UPD")
//		.setBuidCol("BU_ID")//
		.setNormalCol("NAME")
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
				.andExpect(jsonPath("$.workflowUri", notNullValue()))
				.andExpect(jsonPath("$.workflowStatus", is(("Processed"))));

	}


}
