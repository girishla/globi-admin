package com.globi.infa.generator.ptp;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.globi.infa.datasource.core.TableColumnMetadataVisitor;
import com.globi.infa.datasource.core.TableColumnRepository;
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

		
		List<PTPWorkflowSourceColumn> workflowSourceColumnList=	allCols.stream()
		.map(colName->{
			
			
			return PTPWorkflowSourceColumn.builder()//
					.integrationIdColumn(integrationIdCols.stream().anyMatch(col -> col.equals(colName)))//
					.buidColumn(buidCols.stream().anyMatch(col -> col.equals(colName)))//
					.pguidColumn(pguidCols.stream().anyMatch(col -> col.equals(colName)))//
					.changeCaptureColumn(colName.equals(changeCaptureCol))//
					.columnSequence(0)
					.sourceColumnName(colName)//
					.build();
			

			
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
