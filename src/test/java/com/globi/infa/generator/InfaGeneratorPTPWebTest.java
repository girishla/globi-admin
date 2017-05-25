package com.globi.infa.generator;

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

public class InfaGeneratorPTPWebTest extends AbstractWebIntegrationTest {

	@Autowired
	PTPWorkflowRepository wfRepository;
	
	@Test
	public void createsPTPWorkflowViaResource() throws Exception {

		final String sourceTable = "S_ORG_EXT";
		final String source = "SBL";

		PTPWorkflow ptpWorkflow = PTPWorkflow.builder()//
				.sourceName(source)//
				.sourceTableName(sourceTable)
				.workflow(InfaWorkflow.builder()//
						.workflowScmUri("/GeneratedWorkflows/Repl/" + "PTP_" + sourceTable + ".xml")//
						.workflowName("PTP_" + sourceTable + "_Extract")//
						.workflowType("PTP")//
						.build())
				.build();
				
	

		mvc.perform(post("/infagen/workflows/ptp")//
				.content(asJsonString(ptpWorkflow))//
				.contentType(MediaType.APPLICATION_JSON_VALUE)//
				.accept(MediaType.APPLICATION_JSON_VALUE))//
				.andDo(MockMvcResultHandlers.print())//
				.andExpect(status().isCreated())//
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));//
//				.andExpect(jsonPath("$.workflowName", notNullValue()))//
//				.andExpect(jsonPath("$.workflowScmUri", notNullValue()));

	}

}
