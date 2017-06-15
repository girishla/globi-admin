package com.globi.infa.generator;

import static com.globi.infa.generator.StaticObjectMother.getCCColumn;
import static com.globi.infa.generator.StaticObjectMother.getIntegrationIdColumn;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.globi.AbstractWebIntegrationTest;
import com.globi.infa.workflow.InfaPTPWorkflowRepository;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowSourceColumn;

public class PTPPrimaryWorkflowWebtest extends AbstractWebIntegrationTest {

	@Autowired
	InfaPTPWorkflowRepository wfRepository;
	PTPWorkflow ptpWorkflow;
	static final String sourceTable = "S_PARTY_PER";
	static final String source = "CUK";

	@Before
	public void setup(){ 

		List<PTPWorkflowSourceColumn> cols=new ArrayList<>();
		cols.add(getIntegrationIdColumn("ROW_ID"));
		cols.add(getCCColumn("LAST_UPD"));

		
		ptpWorkflow = PTPWorkflow.builder()//
				.sourceName(source)//
				.columns(cols)
				.sourceTableName(sourceTable)//
				.workflowUri("/GeneratedWorkflows/Repl/" + "PTP_" + source+ "_"+ sourceTable  + ".xml")
//				.workflowType("PTP")
				.workflowName("PTP_" + source+ "_"+ sourceTable  + "_Extract")
				.build();
	}
	
	@Test
	public void createsWorkflowResourceFromWorkflowDefinition() throws Exception {

		mvc.perform(post("/infagen/workflows/ptp")//
				.content(asJsonString(ptpWorkflow))//
				.contentType(MediaType.APPLICATION_JSON_VALUE)//
				.accept(MediaType.APPLICATION_JSON_VALUE))//
				.andDo(MockMvcResultHandlers.print())//
				.andExpect(status().isCreated())//
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))//
				.andExpect(jsonPath("$.workflowName", notNullValue()))//
				.andExpect(jsonPath("$.workflowUri", notNullValue()));

	}

}
