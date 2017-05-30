package com.globi.infa.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.globi.infa.datasource.core.DataSourceTableColumnDTO;
import com.globi.infa.datasource.core.DataSourceTableColumnRepository;
import com.globi.infa.datasource.core.DataSourceTableDTO;
import com.globi.infa.generator.FileWriterEventListener;
import com.globi.infa.generator.GitWriterEventListener;
import com.globi.infa.generator.PTPExtractGenerationStrategy;
import com.globi.infa.generator.PTPPrimaryGenerationStrategy;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class InfaPTPWorkflowFromMetadataController {

	@Autowired
	private PTPWorkflowRepository repository;

	@Autowired
	FileWriterEventListener fileWriter;

	@Autowired
	GitWriterEventListener gitWriter;

	@Autowired
	private PTPExtractGenerationStrategy ptpExtractgenerator;

	@Autowired
	private PTPPrimaryGenerationStrategy ptpPrimarygenerator;

	@Autowired
	DataSourceTableColumnRepository metadataColumnRepository;

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/workflows/ptpFromMetadata")
	public @ResponseBody ResponseEntity<?> createPTPExtractWorkflow() {

		List<PTPWorkflow> createdWorkflows = new ArrayList<>();

		List<DataSourceTableColumnDTO> columns = metadataColumnRepository.getAll();

		// Build Distinct Map of Tables
		Map<String, DataSourceTableDTO> tables = columns.stream().map(column -> {
			return DataSourceTableDTO.builder().sourceName(column.getSourceName()).tableName(column.getTableName())
					.tableOwner(column.getTableOwner()).build();
		}).collect(Collectors.toMap(DataSourceTableDTO::getTableName, p -> p, (p, q) -> p));

		tables.keySet().stream().forEach(table -> {

			// Build input WF definition and run generator once for each table
			log.info("------------------------------------------*");
			log.info("**************beginning to process " + table);

			List<PTPWorkflowSourceColumn> workflowSourceColumnList = new ArrayList<>();

			//all columns in the metadata become input columns to the generator 
			columns.stream()//
					.filter(column -> column.getTableName().equals(table))//
					.forEach(column -> {
						// ugly hack to deal with Siebel sources. to be removed
						// where this metadata is captured elsewhere
						if (column.getColName().equals("ROW_ID")) {
							column.setIntegrationId(true);
						}

						if (column.getColName().equals("LAST_UPD")) {
							column.setChangeCaptureCol(true);
						}

						workflowSourceColumnList.add(PTPWorkflowSourceColumn.builder()//
								.changeCaptureColumn(column.isChangeCaptureCol())//
								.integrationIdColumn(column.isIntegrationId())//
								.sourceColumnName(column.getColName())//
								.build());

					});

			PTPWorkflow ptpWorkflow = PTPWorkflow.builder()//
					.sourceName(tables.get(table).getSourceName())//
					.sourceTableName(tables.get(table).getTableName()).columns(workflowSourceColumnList)
					.workflow(InfaWorkflow.builder()//
							.workflowUri(
									"/GeneratedWorkflows/Repl/" + "PTP_" + tables.get(table).getTableName() + ".xml")//
							.workflowName("PTP_" + tables.get(table).getTableName() + "_Extract")//
							.workflowType("PTP")//
							.build())
					.build();

			log.info("************************************************");
			log.info(ptpWorkflow.toString());

			ptpExtractgenerator.setWfDefinition(ptpWorkflow);
			ptpExtractgenerator.addListener(fileWriter);
			ptpExtractgenerator.addListener(gitWriter);
			ptpExtractgenerator.generate();

			createdWorkflows.add(repository.save(ptpWorkflow));

		});

		return new ResponseEntity<List<PTPWorkflow>>(createdWorkflows, HttpStatus.CREATED);
	}

}
