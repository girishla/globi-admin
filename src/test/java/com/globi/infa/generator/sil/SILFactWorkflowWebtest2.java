package com.globi.infa.generator.sil;

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
import com.globi.infa.workflow.sil.InfaSILWorkflowRepository;
import com.globi.infa.workflow.sil.SILWorkflow;
import com.globi.infa.workflow.sil.SILWorkflowSourceColumn;

public class SILFactWorkflowWebtest2 extends AbstractWebIntegrationTest {

	@Autowired
	InfaSILWorkflowRepository wfRepository;

	@Autowired
	SilMetadataRepository silMetadataRepo;

	SILWorkflow silWorkflow;

	@Before
	public void setup() {

		List<SilMetadata> silMetadata = silMetadataRepo.getAll("OPTY_LN");

		List<SILWorkflowSourceColumn> cols = silMetadata.stream()
				.filter(col -> col.isStageColumnFlag() && (col.getColumnType().equals("Foreign Key")
						|| col.getColumnType().equals("Measure Attribute") || col.getColumnType().equals("Measure")
						|| col.getColumnName().equals("DATASOURCE_NUM_ID")))
				.map(col -> {

					return SILWorkflowSourceColumn.builder()//
							.columnName(col.getColumnName())//
							.autoColumn(false)//
							.columnType(col.getColumnType())//
							.dimTableName(col.getDimTableName()).columnOrder(Integer.parseInt(col.getColumnOrder()))
							.domainLookupColumn(false)//
							.legacyColumn(false)//
							.miniDimColumn(false)//
							.targetColumn(false)//
							.build();

				}).collect(Collectors.toList());

		silWorkflow = SILWorkflow.builder()//
				.columns(cols)//
				.loadType("Fact")//
				.stageName("X_OPTY_LN")//
				.tableName("OPTY_LN")//
				.workflowName("SIL_OPTY_LN_Fact")//
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
