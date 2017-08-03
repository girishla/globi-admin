package com.globi.infa.generator.ptp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.globi.infa.datasource.core.TableColumnMetadataVisitor;
import com.globi.infa.datasource.core.TableColumnRepository;
import com.globi.infa.datasource.fbm.FBMTableColumnRepository;
import com.globi.infa.datasource.type.oracle.OracleTableColumnMetadataVisitor;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.workflow.InfaWorkflow;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowSourceColumn;

import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class PTPGeneratorE2EInputBuilder {

	private TableColumnRepository colRepo;

	private TableColumnMetadataVisitor queryVisitor;

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
	List<String> allCols = new ArrayList<>();

	PTPGeneratorE2EInputBuilder(TableColumnRepository colRepo, TableColumnMetadataVisitor queryVisitor) {

		this.colRepo = colRepo;
		this.queryVisitor = queryVisitor;

	}

	public PTPGeneratorE2EInputBuilder setIntegrationCol(String columnName) {
		integrationIdCols.add(columnName);
		allCols.add(columnName);
		return this;
	}

	public PTPGeneratorE2EInputBuilder setBuidCol(String columnName) {
		buidCols.add(columnName);
		allCols.add(columnName);
		return this;
	}

	public PTPGeneratorE2EInputBuilder setPguidCol(String columnName) {
		pguidCols.add(columnName);
		allCols.add(columnName);
		return this;
	}

	public PTPGeneratorE2EInputBuilder setNormalCol(String columnName) {
		allCols.add(columnName);
		return this;
	}

	public PTPGeneratorE2EInputBuilder setChangeCaptureCol(String columnName) {
		this.changeCaptureCol = columnName;
		allCols.add(columnName);
		return this;
	}

	public PTPGeneratorE2EInputBuilder start() {
		sourceName = "";
		tableName = "";
		changeCaptureCol = "";
		integrationIdCols.clear();
		pguidCols.clear();
		buidCols.clear();
		return this;

	}

	public PTPWorkflow build() {

		List<InfaSourceColumnDefinition> columns = colRepo.accept(queryVisitor, tableName);
		int colOrder=0;

		// Build workflow columns DTO from source columns
		List<PTPWorkflowSourceColumn> workflowSourceColumnList = columns.stream()//
				.filter(col -> allCols.stream().anyMatch(allColsColumn -> allColsColumn.equals(col.getColumnName())))
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
							.buidColumn(column.getBuidFlag()).pguidColumn(column.getPguidFlag())
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
				.workflowUri("/GeneratedWorkflows/Repl/" + "PTP_" + sourceName + "_" + tableName + ".xml")
				.workflowName("PTP_" + sourceName + "_" + tableName + "_Extract")//
				.targetTableName(sourceName + "_" + tableName)
				.sourceFilter(sourceFilter)
				.build();

		return ptpWorkflowGeneratorInput;

	}

}
