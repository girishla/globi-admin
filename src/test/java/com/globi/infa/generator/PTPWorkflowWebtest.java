package com.globi.infa.generator;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import com.globi.AbstractWebIntegrationTest;
import com.globi.infa.workflow.InfaWorkflow;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowRepository;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class PTPWorkflowWebtest extends AbstractWebIntegrationTest {

	@Autowired
	PTPWorkflowRepository wfRepository;
	PTPWorkflow ptpWorkflow;
	static final String sourceTable = "S_ORG_EXT";
	static final String source = "SBL";

	@Before
	public void setup(){

		ptpWorkflow = PTPWorkflow.builder()//
				.sourceName(source)//
				.sourceTableName(sourceTable)
				.workflow(InfaWorkflow.builder()//
						.workflowScmUri("/GeneratedWorkflows/ptp/" + "PTP_" + sourceTable + ".xml")//
						.workflowName("PTP_" + sourceTable + "_Extract")//
						.workflowType("PTP")//
						.build())
				.build();
		
	}
	
	
	@Test
	public void createsWorkflowResourceWithoutGeneration() throws Exception {

		mvc.perform(post("/infagen/workflows/ptp")//
				.content(asJsonString(ptpWorkflow))//
				.contentType(MediaType.APPLICATION_JSON_VALUE)//
				.accept(MediaType.APPLICATION_JSON_VALUE))//
				.andDo(MockMvcResultHandlers.print())//
				.andExpect(status().isCreated())//
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))//
				.andExpect(jsonPath("$.workflow.workflowName", notNullValue()))//
				.andExpect(jsonPath("$.workflow.workflowScmUri", notNullValue()));

	}

}
