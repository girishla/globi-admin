package com.globi.infa;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.globi.AbstractWebIntegrationTest;

public class InfaSourceDefinitionWebTest extends AbstractWebIntegrationTest {

//	@Test disabled
	public void createsPIPSourceDefinitionFromSourceTableName() throws Exception {


		Map<String,String> payload = new HashMap<>();
		payload.put("sourceName","CUK");
		payload.put("sourceTableName","S_PARTY");
		
		mvc.perform(post("/infagen/sourceDefinitions")//
				.content(asJsonString(payload))//
				.contentType(MediaType.APPLICATION_JSON)//
				.accept(MediaType.APPLICATION_JSON))//
				.andDo(MockMvcResultHandlers.print())//
				.andExpect(status().isCreated())//
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))//
				.andExpect(jsonPath("$.databaseName", notNullValue()))//
				.andExpect(jsonPath("$.databaseType", notNullValue()))//
				.andExpect(jsonPath("$.sourceTableName", notNullValue()))//
				.andExpect(jsonPath("$.ownerName", notNullValue()));

	}

}
