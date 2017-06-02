package com.globi.infa.generator;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.globi.AbstractWebIntegrationTest;
import com.globi.infa.datasource.core.DataSourceTableColumnDTO;
import com.globi.infa.workflow.InfaWorkflow;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowRepository;
import com.globi.infa.workflow.PTPWorkflowSourceColumn;

public class PTPExtractWorkflowWebtest extends AbstractWebIntegrationTest {

	@Autowired
	PTPWorkflowRepository wfRepository;
	PTPWorkflow ptpWorkflow;
	static final String sourceTable = "R_INVOICE_MASTER";
	static final String source = "GEN";

	@Before
	public void setup(){

		ptpWorkflow = PTPWorkflow.builder()//
				.sourceName(source)//
				.column(new PTPWorkflowSourceColumn("INVOICE_NUMBER",true,false,true))
				.column(new PTPWorkflowSourceColumn("INPUT_DATE",false,true,false))
				.sourceTableName(sourceTable)//
				.workflow(InfaWorkflow.builder()//
						.workflowUri("/GeneratedWorkflows/ptp/" + "PTP_" + sourceTable + ".xml")//
						.workflowName("PTP_" + sourceTable + "_Extract")//
						.workflowType("PTP")//
						.build())
				.build();
	}

	
	@Test @Ignore
	public void createsWorkflowResourceFromWorkflowDefinition() throws Exception {

		mvc.perform(post("/infagen/workflows/ptpExtract")//
				.content(asJsonString(ptpWorkflow))//
				.contentType(MediaType.APPLICATION_JSON_VALUE)//
				.accept(MediaType.APPLICATION_JSON_VALUE))//
				.andDo(MockMvcResultHandlers.print())//
				.andExpect(status().isCreated())//
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))//
				.andExpect(jsonPath("$.workflow.workflowName", notNullValue()))//
				.andExpect(jsonPath("$.workflow.workflowUri", notNullValue()));

	}


}
