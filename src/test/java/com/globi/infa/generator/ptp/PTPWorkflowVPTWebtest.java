package com.globi.infa.generator.ptp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.globi.AbstractWebIntegrationTest;
import com.globi.infa.workflow.ptp.PTPWorkflow;

public class PTPWorkflowVPTWebtest extends AbstractWebIntegrationTest {

	
	PTPWorkflow ptpWorkflow;

	private PTPGeneratorE2EInputBuilder inputBuilder;
	
	
	@Before
	public void setup(){
		inputBuilder= new PTPGeneratorE2EInputBuilder();
		
		ptpWorkflow= inputBuilder.start()//
		.sourceName("VPT")//
		.tableName("AdjustmentGenesis")//
		.setIntegrationCol("RequestID")//
		.setNormalCol("ProcessedDate")
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
