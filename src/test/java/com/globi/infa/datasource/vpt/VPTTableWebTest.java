package com.globi.infa.datasource.vpt;

import static org.hamcrest.CoreMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.globi.AbstractWebIntegrationTest;

public class VPTTableWebTest extends AbstractWebIntegrationTest {

	@Test
	@WithMockUser
	//@Ignore ignore until firewall is opened
	public void exposesVPTTableResource() throws Exception {

		mvc.perform(get("/infagen/datasources/vpt/tables"))//
				.andDo(MockMvcResultHandlers.print()).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$[0].tableOwner", notNullValue()))
				.andExpect(jsonPath("$[0].tableName", notNullValue()));
	}

}
