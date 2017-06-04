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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.globi.AbstractWebIntegrationTest;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowRepository;

public class PTPWorkflowsFromTopDownMetadataWebtest extends AbstractWebIntegrationTest {

	@Autowired
	PTPWorkflowRepository wfRepository;
	PTPWorkflow ptpWorkflow;

	@Before
	public void setup(){

	}
	
	
	@Test
	public void canCreateWorkflowFromExistingTopDownMetadata() throws Exception{
		
		mvc.perform(post("/infagen/workflows/ptpFromMetadata")//
				.content("")//
				.contentType(MediaType.APPLICATION_JSON_VALUE)//
				.accept(MediaType.APPLICATION_JSON_VALUE))//
				.andDo(MockMvcResultHandlers.print())//
				.andExpect(status().isCreated())//
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))//
				.andExpect(jsonPath("$[0].workflow.workflowName", notNullValue()))//
				.andExpect(jsonPath("$[0].workflow.workflowUri", notNullValue()));
		
		
	}
	
	

}
