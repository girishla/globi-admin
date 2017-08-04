package com.globi.infa.generator.sil;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.globi.AbstractWebIntegrationTest;
import com.globi.infa.workflow.InfaSILWorkflowRepository;
import com.globi.infa.workflow.SILWorkflow;
import com.globi.infa.workflow.SILWorkflowSourceColumn;

public class SILDimensionWorkflowWebtest extends AbstractWebIntegrationTest {

	@Autowired
	InfaSILWorkflowRepository wfRepository;

	SILWorkflow silWorkflow;

	@Before
	public void setup() {

		List<SILWorkflowSourceColumn> cols = new ArrayList<>();

		cols.add(SILWorkflowSourceColumn.builder()//
				.columnName("ROW_WID")//
				.columnType("Primary Key")//
				.domainLookupColumn(false)//
				.autoColumn(true)//
				.legacyColumn(true)//
				.miniDimColumn(true)//
				.targetColumn(true)
				.build());

		
		silWorkflow = SILWorkflow.builder()//
				.columns(cols)//
				.loadType("Dimension")//
				.stageName("INVOICE_LN")//
				.tableName("INVOICE_LN")//
				.workflowName("SIL_INVOICE_LN_Dimension")//
				.workflowStatus("Queued")//
				.workflowUri("")//
				.build();

	}

	@Test
	@WithMockUser
	public void createsWorkflowResourceFromWorkflowDefinition() throws Exception {

		mvc.perform(post("/infagen/workflows/sil?sync=true")//
				.content(asJsonString(silWorkflow))//
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
