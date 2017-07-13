package com.globi.infa.datasource.vpt;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.globi.AbstractWebIntegrationTest;

public class VPTTableColumnWebTest extends AbstractWebIntegrationTest {

	@Test
	@WithMockUser
	@Ignore //ignore until firewall is opened
	public void exposesGenesisInvoiceMasterColumnsResource() throws Exception {

		mvc.perform(get("/infagen/datasources/vpt/tables/YearOnYearSummary/columns"))//
				.andDo(MockMvcResultHandlers.print()).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))//
				.andExpect(jsonPath("$[0].columnName", notNullValue()))//
				.andExpect(jsonPath("$[0].columnDataType", notNullValue()));
	}

	

}
