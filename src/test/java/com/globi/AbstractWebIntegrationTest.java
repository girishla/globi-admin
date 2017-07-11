package com.globi;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.util.Locale;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.LinkDiscoverers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base class to derive concrete web test classes from.
 * 
 * @author Girish Lakshmanan
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractWebIntegrationTest {

	@Autowired
	protected WebApplicationContext context;
	@Autowired
	LinkDiscoverers links;

	protected MockMvc mvc;

	@Before
	public void setUpBase() {

		mvc = MockMvcBuilders.webAppContextSetup(context)//
				.apply(springSecurity())//
				.defaultRequest(MockMvcRequestBuilders.get("/").locale(Locale.US)).//
				build();
	}

	protected static String asJsonString(final Object obj) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			final String jsonContent = mapper.writeValueAsString(obj);
			return jsonContent;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}