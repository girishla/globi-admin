package com.globi.infa.metadata.src;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.globi.AbstractWebIntegrationTest;

public class InfaSourceDefinitionPullWebTest extends AbstractWebIntegrationTest{

		
	@Test
	@WithMockUser
	public void pullsAndCreatesSourceDefinitionMetadata() throws Exception{
		
		InfaSourceDefinitionPullDTO sourcePullInput=InfaSourceDefinitionPullDTO.builder()//
				.sourceName("CUK")//
				.tableName("S_CALL_LST")
				.build();
		
		
		mvc.perform(post("/sourcetables/pull")//
				.content(asJsonString(sourcePullInput))//
				.contentType(MediaType.APPLICATION_JSON_VALUE)//
				.accept(MediaType.APPLICATION_JSON_VALUE))//
				.andDo(MockMvcResultHandlers.print())//
				.andExpect(status().isCreated())//
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))//
				.andExpect(jsonPath("$.sourceTableName", notNullValue()))//
				.andExpect(jsonPath("$.databaseType", notNullValue()));
		
		
	}
	
	
	
	
}
