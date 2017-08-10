package com.globi.infa.generator.sil;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.globi.AbstractWebIntegrationTest;
import com.globi.infa.metadata.sil.TopDownSILTableDefn;

public class SILTopDownGeneratorWebTest extends AbstractWebIntegrationTest {

	@Test
	@WithMockUser
	public void createsWorkflowsFromTableNameAndLoadType() throws Exception {

		List<TopDownSILTableDefn> inputTables = new ArrayList<>();
		
		inputTables.add(TopDownSILTableDefn.builder()//
				.loadType("dimension")//
				.tableName("INVOICE_LN")//
				.build());
		
		
		mvc.perform(post("/infagen/workflows/silFromMetadata?sync=true")//
				.content(asJsonString(inputTables))//
				.contentType(MediaType.APPLICATION_JSON_VALUE)//
				.accept(MediaType.APPLICATION_JSON_VALUE))//
				.andDo(MockMvcResultHandlers.print())//
				.andExpect(status().isCreated())// TN
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))//
				.andExpect(jsonPath("$[0].workflowName", notNullValue()))//
				.andExpect(jsonPath("$[0].workflowUri", notNullValue()))
				.andExpect(jsonPath("$[0].workflowStatus", is(("Processed"))));

	}

}
