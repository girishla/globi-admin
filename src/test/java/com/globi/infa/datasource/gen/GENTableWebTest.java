package com.globi.infa.datasource.gen;

import static org.hamcrest.CoreMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.globi.AbstractWebIntegrationTest;

public class GENTableWebTest extends AbstractWebIntegrationTest {

	@Test
	public void exposesGENTableResource() throws Exception {

		mvc.perform(get("/infagen/datasources/gen/tables"))//
				.andDo(MockMvcResultHandlers.print()).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$[0].tableOwner", notNullValue()))
				.andExpect(jsonPath("$[0].tableName", notNullValue()));
	}

}
