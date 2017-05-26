package com.globi.infa.datasource.gen;

import static org.hamcrest.CoreMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.globi.AbstractWebIntegrationTest;

public class GENTableColumnWebTest extends AbstractWebIntegrationTest {

	@Test
	public void exposesGenesisInvoiceMasterColumnsResource() throws Exception {

		mvc.perform(get("/infagen/datasources/gen/tables/R_INVOICE_MASTER/columns"))//
				.andDo(MockMvcResultHandlers.print()).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))//
				.andExpect(jsonPath("$[0].columnName", notNullValue()))//
				.andExpect(jsonPath("$[0].columnDataType", notNullValue()));
	}

	
	@Test
	public void exposesGenesisInvoiceMasterTableColumnsWhenPassedTableInLowerCase() throws Exception {

		mvc.perform(get("/infagen/datasources/gen/tables/r_invoice_master/columns"))//
				.andDo(MockMvcResultHandlers.print()).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))//
				.andExpect(jsonPath("$[0].columnName", notNullValue()))//
				.andExpect(jsonPath("$[0].columnDataType", notNullValue()));
	}

	
}
