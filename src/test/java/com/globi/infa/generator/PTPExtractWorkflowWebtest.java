package com.globi.infa.generator;

import static com.globi.infa.generator.StaticObjectMother.getBuidColumn;
import static com.globi.infa.generator.StaticObjectMother.getCCColumn;
import static com.globi.infa.generator.StaticObjectMother.getIntegrationIdColumn;
import static com.globi.infa.generator.StaticObjectMother.getNormalColumn;
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

public class PTPExtractWorkflowWebtest extends AbstractWebIntegrationTest {

	@Autowired
	InfaPTPWorkflowRepository wfRepository;
	PTPWorkflow ptpWorkflow;
	static final String sourceTable = "S_CONTACT";
	static final String source = "CUK";

	@Before
	public void setup(){
		
/*		List<PTPWorkflowSourceColumn> cols=new ArrayList<>();
		cols.add(getIntegrationIdColumn("INVOICE_NUMBER"));
		cols.add(getCCColumn("INPUT_DATE"));*/
		

		List<PTPWorkflowSourceColumn> cols=new ArrayList<>();
		cols.add(getIntegrationIdColumn("ROW_ID"));
		cols.add(getCCColumn("LAST_UPD"));
		cols.add(getNormalColumn("LAST_NAME"));
		cols.add(getBuidColumn("BU_ID"));
		

		ptpWorkflow = PTPWorkflow.builder()//
				.sourceName(source)//
				.columns(cols)
				.sourceTableName(sourceTable)//
				.workflowUri("/GeneratedWorkflows/Repl/" + "PTP_" + source+ "_"+ sourceTable + ".xml")
				.workflowName("PTP_" + source+ "_"+ sourceTable  + "_Extract")
				.build();
		
	}

	
	@Test
	public void createsWorkflowResourceFromWorkflowDefinition() throws Exception {

		mvc.perform(post("/infagen/workflows/ptp?sync=true")//
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
