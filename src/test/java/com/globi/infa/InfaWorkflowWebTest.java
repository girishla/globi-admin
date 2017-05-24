package com.globi.infa;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.globi.AbstractWebIntegrationTest;
import com.globi.infa.DataSourceTable;

public class InfaWorkflowWebTest extends AbstractWebIntegrationTest {

	@Test
	public void createsReplWorkflowFromSourceTableName() throws Exception {

		DataSourceTable dsTable=new DataSourceTable("SIEBEL","S_ORG_EXT");
		
		mvc.perform(post("/infagen/workflows")//
				.content(asJsonString(dsTable))//
				.contentType(MediaType.APPLICATION_JSON)//
				.accept(MediaType.APPLICATION_JSON))//
				.andDo(MockMvcResultHandlers.print())//
				.andExpect(status().isCreated())//
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))//
				.andExpect(jsonPath("$.workflowName", notNullValue()))//
				.andExpect(jsonPath("$.workflowScmUri", notNullValue()));

	}

}
