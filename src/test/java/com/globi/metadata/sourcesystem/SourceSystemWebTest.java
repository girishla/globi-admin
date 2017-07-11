package com.globi.metadata.sourcesystem;


import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Test;
import org.springframework.hateoas.MediaTypes;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.globi.AbstractWebIntegrationTest;


public class SourceSystemWebTest extends AbstractWebIntegrationTest {

	@Test
	@WithMockUser
	public void exposesSourceSystemResource() throws Exception {

		
		mvc.perform(get("/sourcesystems"))//
				.andDo(MockMvcResultHandlers.print()).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON))
				.andExpect(jsonPath("$._embedded.sourcesystems[0].name", notNullValue()))
				.andExpect(jsonPath("$._embedded.sourcesystems[0].dbType", notNullValue()));

	}

}
