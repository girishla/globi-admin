package com.globi.infa.datasource.lnicrm;


import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.globi.AbstractWebIntegrationTest;

public class LNICRMDatasourceWebTest extends AbstractWebIntegrationTest {

	@Test
	@WithMockUser
	@Ignore//taking too long
	public void exposesLNICRMTableResource() throws Exception {

		mvc.perform(get("/infagen/datasources/cuk/tables"))//
				.andDo(MockMvcResultHandlers.print()).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("application/json"))
				.andExpect(jsonPath("$[0].tableOwner", notNullValue()))
				.andExpect(jsonPath("$[0].tableName", notNullValue()));
	}

}
