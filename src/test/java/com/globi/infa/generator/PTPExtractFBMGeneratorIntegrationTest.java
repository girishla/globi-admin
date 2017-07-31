package com.globi.infa.generator;

import static org.junit.Assert.assertThat;
import static org.xmlunit.matchers.HasXPathMatcher.hasXPath;

import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.annotation.Rollback;

import com.globi.AbstractIntegrationTest;
import com.globi.infa.datasource.fbm.FBMTableColumnRepository;
import com.globi.infa.datasource.type.oracle.OracleTableColumnMetadataVisitor;
import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.generator.ptp.PTPGenerationStrategy;
import com.globi.infa.workflow.InfaPTPWorkflowRepository;
import com.globi.infa.workflow.PTPWorkflow;

public class PTPExtractFBMGeneratorIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private Jaxb2Marshaller marshaller;

	@Autowired
	private PTPGenerationStrategy generator;

	@Autowired
	InfaPTPWorkflowRepository repository;

	@Autowired
	FileWriterEventListener fileWriter;

	@Autowired
	PTPRepositoryWriterEventListener repoWriter;

	@Autowired
	GitWriterEventListener gitWriter;

	private PTPGeneratorInputBuilder inputBuilder;
	
	
	@Autowired
	private FBMTableColumnRepository colRepo;
	
	@Autowired
	private OracleTableColumnMetadataVisitor queryVisitor; 
	
	

	PTPWorkflow ptpWorkflowGeneratorInvoiceHDInput;

	PTPWorkflow ptpWorkflowGeneratorInvoiceLNInput;

	PTPWorkflow ptpWorkflowGeneratorASMSRYInput;

	PTPWorkflow ptpWorkflowGeneratorASMDTLInput;

	
	
	
	@Before
	public void setup() {

		
		inputBuilder=new PTPGeneratorInputBuilder(colRepo,queryVisitor);
		
		ptpWorkflowGeneratorInvoiceHDInput = inputBuilder.start()//
				.sourceName("FBM")//
				.tableName("PS_LN_BI_INV_HD_VW")//
				.setIntegrationCol("INVOICE")//
				.setBuidCol("BUSINESS_UNIT")//
				.setPguidCol("INVOICE")//
				.changeCaptureCol("LAST_UPDATE_DTTM")//
				.sourceFilter("PS_LN_BI_INV_HD_VW.BUSINESS_UNIT IN ('00AU0', '00NZ0', '00UK1')").build();

		ptpWorkflowGeneratorInvoiceLNInput = inputBuilder.start()//
				.sourceName("FBM")//
				.tableName("PS_LN_BI_INV_LN_VW")//
				.setIntegrationCol("INVOICE")//
				.setIntegrationCol("LINE_SEQ_NUM")//
				.setBuidCol("BUSINESS_UNIT")//
				.setPguidCol("INVOICE")//
				.setPguidCol("LINE_SEQ_NUM")//
//				.changeCaptureCol("LAST_UPDATE_DTTM")//
				.sourceFilter("PS_LN_BI_INV_LN_VW.BUSINESS_UNIT IN ('00AU0', '00NZ0', '00UK1')").build();

		
		ptpWorkflowGeneratorASMSRYInput = inputBuilder.start()//
				.sourceName("FBM")//
				.tableName("PS_LN_CA_ASMSRY_VW")//
				.setBuidCol("BUSINESS_UNIT")//
				.changeCaptureCol("LASTUPDDTTM")//
				.sourceFilter("PS_LN_CA_ASMSRY_VW.BUSINESS_UNIT IN ('00AU0', '00NZ0', '00UK1')").build();

		
		ptpWorkflowGeneratorASMDTLInput = inputBuilder.start()//
				.sourceName("FBM")//
				.tableName("PS_LN_CA_ASMDTL_VW")//
				.setBuidCol("BUSINESS_UNIT")//
				.changeCaptureCol("LASTUPDDTTM")//
				.sourceFilter("PS_LN_CA_ASMDTL_VW.BUSINESS_UNIT IN ('00AU0', '00NZ0', '00UK1')").build();

	}

	private InfaPowermartObject generateAndSave(PTPWorkflow wfInput) {

		generator.addListener(fileWriter);
		generator.addListener(gitWriter);
		InfaPowermartObject pmObj = generator.generate(wfInput);

		Optional<PTPWorkflow> existingWorkflow = repository
				.findByWorkflowName(ptpWorkflowGeneratorInvoiceHDInput.getWorkflow().getWorkflowName());
		if (existingWorkflow.isPresent()) {
			repository.delete(existingWorkflow.get());
		}
		repository.save(ptpWorkflowGeneratorInvoiceHDInput);

		return pmObj;

	}

	private void assertContentOk(InfaPowermartObject pmObj) throws Exception {

		String testXML = asString(marshaller.getJaxbContext(), pmObj.pmObject);

		assertThat(testXML, (hasXPath("/POWERMART/REPOSITORY/FOLDER/SOURCE")));
		assertThat(testXML, (hasXPath("/POWERMART/REPOSITORY/FOLDER/TARGET")));
		assertThat(testXML, (hasXPath("/POWERMART/REPOSITORY/FOLDER/MAPPING")));
		assertThat(testXML, (hasXPath("/POWERMART/REPOSITORY/FOLDER/CONFIG")));
		assertThat(testXML, (hasXPath("/POWERMART/REPOSITORY/FOLDER/WORKFLOW")));
		assertThat(testXML, (hasXPath("/POWERMART/REPOSITORY/FOLDER/MAPPLET")));

		assertThat(testXML, (hasXPath(
				"/POWERMART/REPOSITORY/FOLDER/MAPPING/INSTANCE[@NAME='SQ_ExtractData']/ASSOCIATED_SOURCE_INSTANCE")));
	}

	@Test
	@Rollback(false)
	

	public void generatesPTPWorkflowForFBMView() throws Exception {

		InfaPowermartObject pmObj;
//Dont run because view IS missing in PSFT
//		pmObj = generateAndSave(ptpWorkflowGeneratorInvoiceHDInput);
//		assertContentOk(pmObj);
		pmObj = generateAndSave(ptpWorkflowGeneratorInvoiceLNInput);
		assertContentOk(pmObj);

		//		pmObj = generateAndSave(ptpWorkflowGeneratorASMSRYInput);
//		assertContentOk(pmObj);
//		pmObj = generateAndSave(ptpWorkflowGeneratorASMDTLInput);
//		assertContentOk(pmObj);

	}

}
