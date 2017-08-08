package com.globi.infa.generator.sil;

import static com.globi.infa.generator.sil.SILStaticObjectMother.getSpecialColumn;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.globi.AbstractWebIntegrationTest;
import com.globi.infa.metadata.sil.SilMetadata;
import com.globi.infa.metadata.sil.SilMetadataRepository;
import com.globi.infa.workflow.InfaSILWorkflowRepository;
import com.globi.infa.workflow.SILWorkflow;
import com.globi.infa.workflow.SILWorkflowSourceColumn;

public class SILFactWorkflowWebtest extends AbstractWebIntegrationTest {

	@Autowired
	InfaSILWorkflowRepository wfRepository;

	@Autowired
	SilMetadataRepository silMetadataRepo;

	SILWorkflow silWorkflow;

	@Before
	public void setup() {

		List<SilMetadata> silMetadata = silMetadataRepo.getAll("INVOICE_LN");

		List<SILWorkflowSourceColumn> cols = silMetadata.stream().filter(col -> col.isStageColumnFlag()).map(col -> {

			return getSpecialColumn(col.getColumnName(), col.getColumnType());

		}).collect(Collectors.toList());

		silWorkflow = SILWorkflow.builder()//
				.columns(cols)//
				.loadType("Fact")//
				.stageName("X_INVOICE_LN")//
				.tableName("INVOICE_LN")//
				.workflowName("SIL_INVOICE_LN_Fact")//
				.workflowStatus("Queued")//
				.workflowUri("")//
				.build();

	}

	@Test
	@WithMockUser
	public void createsWorkflowResourceFromWorkflowDefinition() throws Exception {

		mvc.perform(post("/infagen/workflows/sil/fact?sync=true")//
				.content(asJsonString(silWorkflow))//
				.contentType(MediaType.APPLICATION_JSON_VALUE)//
				.accept(MediaType.APPLICATION_JSON_VALUE))//
				.andDo(MockMvcResultHandlers.print())//
				.andExpect(status().isCreated())// TN
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))//
				.andExpect(jsonPath("$.workflowName", notNullValue()))//
				.andExpect(jsonPath("$.workflowUri", notNullValue()))
				.andExpect(jsonPath("$.workflowStatus", is(("Processed"))));

	}

}
