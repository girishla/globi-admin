package com.globi.infa.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.globi.infa.datasource.core.OracleTableColumnMetadataVisitor;
import com.globi.infa.datasource.fbm.FBMTableColumnRepository;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.workflow.InfaWorkflow;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowSourceColumn;

import lombok.Setter;
import lombok.experimental.Accessors;

@Component
@Scope("prototype")
@Accessors(fluent = true)
public class PTPGeneratorInputBuilder {

	@Autowired
	private FBMTableColumnRepository fbmColrepository;

	@Autowired
	private OracleTableColumnMetadataVisitor oraColumnQueryVisitor;

	private PTPWorkflow ptpWorkflowGeneratorInput;

	@Setter
	private String sourceName;
	@Setter
	private String tableName;
	@Setter
	private String changeCaptureCol;
	@Setter
	private String sourceFilter;

	List<String> integrationIdCols = new ArrayList<>();
	List<String> pguidCols = new ArrayList<>();
	List<String> buidCols = new ArrayList<>();

	public PTPGeneratorInputBuilder setIntegrationCol(String columnName) {
		integrationIdCols.add(columnName);
		return this;
	}

	public PTPGeneratorInputBuilder setBuidCol(String columnName) {
		buidCols.add(columnName);
		return this;
	}

	public PTPGeneratorInputBuilder setPguidCol(String columnName) {
		pguidCols.add(columnName);
		return this;
	}

	public PTPGeneratorInputBuilder start() {
		sourceName="";
		tableName="";
		changeCaptureCol="";
		integrationIdCols.clear();
		pguidCols.clear();
		buidCols.clear();
		return this;

	}

	public PTPWorkflow build() {

		List<InfaSourceColumnDefinition> columns = fbmColrepository.accept(oraColumnQueryVisitor, tableName);
		// Build workflow columns DTO from source columns
		List<PTPWorkflowSourceColumn> workflowSourceColumnList = columns.stream()//
				.map(column -> {

					if (integrationIdCols.stream().anyMatch(col -> column.getColumnName().equals(col))) {
						column.setIntegrationIdFlag(true);
					}

					if (pguidCols.stream().anyMatch(col -> column.getColumnName().equals(col))) {
						column.setPguidFlag(true);
					}

					if (buidCols.stream().anyMatch(col -> column.getColumnName().equals(col))) {
						column.setBuidFlag(true);
					}

					if (column.getColumnName().equals(changeCaptureCol)) {
						column.setCcFlag(true);
					}
					
					
					PTPWorkflowSourceColumn wfCol = PTPWorkflowSourceColumn.builder()//
							.integrationIdColumn(column.getIntegrationIdFlag())//
							.buidColumn(column.getBuidFlag())
							.pguidColumn(column.getPguidFlag())
							.changeCaptureColumn(column.getCcFlag())//
							.sourceColumnName(column.getColumnName())//
							.build();

					return wfCol;
				}).collect(Collectors.toList());

		// Build definition to be passed as input to generator
		ptpWorkflowGeneratorInput = PTPWorkflow.builder()//
				.sourceName(sourceName)//
				.sourceTableName(tableName)//
				.columns(workflowSourceColumnList)
				.workflow(InfaWorkflow.builder()//
						.workflowUri("/GeneratedWorkflows/Repl/" + "PTP_" + tableName + ".xml")//
						.workflowName("PTP_" + sourceName + "_" + tableName + "_Extract")//
						.workflowType("PTP")//
						.build())
				.sourceFilter(sourceFilter)
				.build();

		return ptpWorkflowGeneratorInput;

	}

}
