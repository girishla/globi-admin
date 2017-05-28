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
import com.globi.infa.workflow.InfaWorkflow;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowRepository;
import com.globi.infa.workflow.PTPWorkflowSourceColumn;

public class PTPPrimaryWorkflowWebtest extends AbstractWebIntegrationTest {

	@Autowired
	PTPWorkflowRepository wfRepository;
	PTPWorkflow ptpWorkflow;
	static final String sourceTable = "S_ORG_EXT";
	static final String source = "CUK";

	@Before
	public void setup(){

		ptpWorkflow = PTPWorkflow.builder()//
				.sourceName(source)//
				.column(new PTPWorkflowSourceColumn("ROW_ID",true,false))
				.column(new PTPWorkflowSourceColumn("LAST_UPD",false,true))
				.sourceTableName(sourceTable)//
				.workflow(InfaWorkflow.builder()//
						.workflowScmUri("/GeneratedWorkflows/ptp/" + "PTP_" + sourceTable + ".xml")//
						.workflowName("PTP_" + sourceTable + "_Primary")//
						.workflowType("PTP")//
						.build())
				.build();
		
	}
	
	@Test
	public void createsWorkflowResourceFromWorkflowDefinition() throws Exception {

		mvc.perform(post("/infagen/workflows/ptpPrimary")//
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
