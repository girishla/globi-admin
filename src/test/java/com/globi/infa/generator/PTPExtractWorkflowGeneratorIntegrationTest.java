package com.globi.infa.generator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.annotation.Rollback;
import org.xml.sax.SAXException;

import com.globi.AbstractIntegrationTest;
import com.globi.infa.datasource.core.InfaSourceColumnDefinition;
import com.globi.infa.datasource.core.OracleTableColumnMetadataVisitor;
import com.globi.infa.datasource.fbm.FBMTableColumnRepository;
import com.globi.infa.workflow.InfaWorkflow;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowRepository;
import com.globi.infa.workflow.PTPWorkflowSourceColumn;

import xjc.POWERMART;

public class PTPExtractWorkflowGeneratorIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private Jaxb2Marshaller marshaller;

	@Autowired
	private PTPExtractGenerationStrategy generator;

	@Autowired
	PTPWorkflowRepository wfRepository;
	PTPWorkflow ptpWorkflow;

	@Autowired
	FileWriterEventListener fileWriter;

	@Autowired
	GitWriterEventListener gitWriter;

	@Autowired
	private FBMTableColumnRepository fbmColrepository;

	@Autowired
	private OracleTableColumnMetadataVisitor oraColumnQueryVisitor;

	@Before
	public void setup() {

	}

	private String asString(JAXBContext pContext, Object pObject) throws JAXBException {

		java.io.StringWriter sw = new StringWriter();

		Marshaller jaxMarshaller = pContext.createMarshaller();
		jaxMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		jaxMarshaller.marshal(pObject, sw);

		return sw.toString();
	}

	private POWERMART loadControlFileAsObject(String controlFileName) throws FileNotFoundException, IOException {

		FileInputStream is = null;

		try {
			Resource resource = new ClassPathResource("test/" + controlFileName + ".xml");
			is = new FileInputStream(resource.getFile());
			return (POWERMART) marshaller.unmarshal(new StreamSource(is));
		} finally {
			if (is != null) {
				is.close();
			}
		}

	}

	@Test @Ignore
	@Rollback(false)
	public void generatesPTPWorkflowForOrgExtTable()
			throws JAXBException, FileNotFoundException, IOException, SAXException {

		String sourceTable = "S_ORG_EXT";
		String source = "CUK";

		ptpWorkflow = PTPWorkflow.builder()//
				.sourceName(source)//
				.sourceTableName(sourceTable)//
				.column(new PTPWorkflowSourceColumn("ROW_ID", true, false))
				.column(new PTPWorkflowSourceColumn("LAST_UPD", false, true))
				.column(new PTPWorkflowSourceColumn("NAME", false, false))
				.workflow(InfaWorkflow.builder()//
						.workflowUri("/GeneratedWorkflows/Repl/" + "PTP_" + sourceTable + ".xml")//
						.workflowName("PTP_" + sourceTable + "_Extract")//
						.workflowType("PTP")//
						.build())
				.build();

		generator.setWfDefinition(ptpWorkflow);
		generator.addListener(fileWriter);
		generator.addListener(gitWriter);

		InfaPowermartObject pmObj = generator.generate();

		String testXML = asString(marshaller.getJaxbContext(), pmObj.pmObject);
		POWERMART controlObj = loadControlFileAsObject("PTP_S_ORG_EXT_Extract");
		String controlXML = asString(marshaller.getJaxbContext(), controlObj);
		wfRepository.save(ptpWorkflow);
		assertXMLEqual(controlXML, testXML);

	}

	@Test @Ignore
	@Rollback(false)
	public void generatesPTPWorkflowForGenesisInvoiceMaster()
			throws JAXBException, FileNotFoundException, IOException, SAXException {

		String sourceTable = "R_INVOICE_MASTER";
		String source = "GEN";

		PTPWorkflow ptpWorkflow = PTPWorkflow.builder()//
				.sourceName(source)//
				.sourceTableName(sourceTable).column(new PTPWorkflowSourceColumn("INVOICE_NUMBER", true, false))
				.column(new PTPWorkflowSourceColumn("INPUT_DATE", false, true))
				.column(new PTPWorkflowSourceColumn("ORDER_REFERENCE", false, false))
				.workflow(InfaWorkflow.builder()//
						.workflowUri("/GeneratedWorkflows/Repl/" + "PTP_" + sourceTable + ".xml")//
						.workflowName("PTP_" + sourceTable + "_Extract")//
						.workflowType("PTP")//
						.build())
				.build();

		generator.setWfDefinition(ptpWorkflow);
		generator.addListener(fileWriter);
		generator.addListener(gitWriter);
		InfaPowermartObject pmObj = generator.generate();

		String testXML = asString(marshaller.getJaxbContext(), pmObj.pmObject);
		POWERMART controlObj = loadControlFileAsObject("PTP_R_INVOICE_MASTER_Extract");
		String controlXML = asString(marshaller.getJaxbContext(), controlObj);

		wfRepository.save(ptpWorkflow);
		assertXMLEqual(controlXML, testXML);

	}

	@Test
	@Rollback(false)
	public void generatesPTPWorkflowForFBMView1()
			throws JAXBException, FileNotFoundException, IOException, SAXException {

		String sourceTable = "PS_LN_BI_INV_LN_VW";
		String source = "FBM";

		List<InfaSourceColumnDefinition> columns = fbmColrepository.accept(oraColumnQueryVisitor, sourceTable);

		// Build workflow columns DTO from source columns
		List<PTPWorkflowSourceColumn> workflowSourceColumnList = columns.stream().map(column -> {

			if (column.getColumnName().equals("INVOICE") || column.getColumnName().equals("LINE_SEQ_NUM")){
				column.setIntegrationIdFlag(true);
			}
			
			PTPWorkflowSourceColumn wfCol = PTPWorkflowSourceColumn.builder()//
					.integrationIdColumn(column.getIntegrationIdFlag())//
					.changeCaptureColumn(false)//
					.sourceColumnName(column.getColumnName())//
					.build();

			return wfCol;
		}).collect(Collectors.toList());

		PTPWorkflow ptpWorkflow = PTPWorkflow.builder()//
				.sourceName(source)//
				.sourceTableName(sourceTable).columns(workflowSourceColumnList)
				.workflow(InfaWorkflow.builder()//
						.workflowUri("/GeneratedWorkflows/Repl/" + "PTP_" + sourceTable + ".xml")//
						.workflowName("PTP_" + sourceTable + "_Extract")//
						.workflowType("PTP")//
						.build())
				.build();

		generator.setWfDefinition(ptpWorkflow);
		generator.addListener(fileWriter);
		generator.addListener(gitWriter);
		InfaPowermartObject pmObj = generator.generate();

		// String testXML = asString(marshaller.getJaxbContext(),
		// pmObj.pmObject);
		// POWERMART controlObj =
		// loadControlFileAsObject("PTP_R_INVOICE_MASTER_Extract");
		// String controlXML = asString(marshaller.getJaxbContext(),
		// controlObj);

		wfRepository.save(ptpWorkflow);
		// assertXMLEqual(controlXML, testXML);

	}
	
	

	@Test
	@Rollback(false)
	public void generatesPTPWorkflowForFBMView2()
			throws JAXBException, FileNotFoundException, IOException, SAXException {

		String sourceTable = "PS_LN_BI_INV_HD_VW";
		String source = "FBM";

		List<InfaSourceColumnDefinition> columns = fbmColrepository.accept(oraColumnQueryVisitor, sourceTable);

		// Build workflow columns DTO from source columns
		List<PTPWorkflowSourceColumn> workflowSourceColumnList = columns.stream().map(column -> {

			if (column.getColumnName().equals("INVOICE")){
				column.setIntegrationIdFlag(true);
			}
			
			PTPWorkflowSourceColumn wfCol = PTPWorkflowSourceColumn.builder()//
					.integrationIdColumn(column.getIntegrationIdFlag())//
					.changeCaptureColumn(false)//
					.sourceColumnName(column.getColumnName())//
					.build();

			return wfCol;
		}).collect(Collectors.toList());

		PTPWorkflow ptpWorkflow = PTPWorkflow.builder()//
				.sourceName(source)//
				.sourceTableName(sourceTable).columns(workflowSourceColumnList)
				.workflow(InfaWorkflow.builder()//
						.workflowUri("/GeneratedWorkflows/Repl/" + "PTP_" + sourceTable + ".xml")//
						.workflowName("PTP_" + sourceTable + "_Extract")//
						.workflowType("PTP")//
						.build())
				.build();

		generator.setWfDefinition(ptpWorkflow);
		generator.addListener(fileWriter);
		generator.addListener(gitWriter);
		InfaPowermartObject pmObj = generator.generate();

		// String testXML = asString(marshaller.getJaxbContext(),
		// pmObj.pmObject);
		// POWERMART controlObj =
		// loadControlFileAsObject("PTP_R_INVOICE_MASTER_Extract");
		// String controlXML = asString(marshaller.getJaxbContext(),
		// controlObj);

		wfRepository.save(ptpWorkflow);
		// assertXMLEqual(controlXML, testXML);

	}

}
