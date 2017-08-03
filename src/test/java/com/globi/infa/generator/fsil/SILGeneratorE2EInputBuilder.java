package com.globi.infa.generator.fsil;



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
import com.globi.infa.workflow.SILWorkflow;
import com.globi.infa.workflow.SILWorkflowSourceColumn;

import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class SILGeneratorE2EInputBuilder {

	private TableColumnRepository colRepo;
	private TableColumnMetadataVisitor queryVisitor;
	private SILWorkflow WorkflowGeneratorInput;

	@Setter
	private String loadType;
	@Setter
	private String tableName;
	@Setter
	private String stageName;

	List<String> targetCols = new ArrayList<>();
	List<String> legacyCols = new ArrayList<>();
	List<String> miniDimCols = new ArrayList<>();
	List<String> domainLookupCols = new ArrayList<>();
	List<String> autoCols = new ArrayList<>();
	List<String> allCols = new ArrayList<>();
	
	SILGeneratorE2EInputBuilder(TableColumnRepository colRepo, TableColumnMetadataVisitor queryVisitor) {

		this.colRepo = colRepo;
		this.queryVisitor = queryVisitor;

	}

	public SILGeneratorE2EInputBuilder setTargetCol(String columnName) {
		targetCols.add(columnName);
		allCols.add(columnName);
		return this;
	}

	public SILGeneratorE2EInputBuilder setLegacyCol(String columnName) {
		legacyCols.add(columnName);
		allCols.add(columnName);
		return this;
	}

	public SILGeneratorE2EInputBuilder setMiniDimCol(String columnName) {
		miniDimCols.add(columnName);
		allCols.add(columnName);
		return this;
	}

	public SILGeneratorE2EInputBuilder setDomainLookupCol(String columnName) {
		domainLookupCols.add(columnName);
		allCols.add(columnName);
		return this;
	}


	public SILGeneratorE2EInputBuilder setAutoCol(String columnName) {
		autoCols.add(columnName);
		allCols.add(columnName);
		return this;
	}
	
	
	public SILGeneratorE2EInputBuilder start() {
		loadType = "";
		tableName = "";
		stageName = "";
		targetCols.clear();
		legacyCols.clear();
		miniDimCols.clear();
		domainLookupCols.clear();
		autoCols.clear();
		allCols.clear();
		return this;

	}

	public SILWorkflow build() {

		List<InfaSourceColumnDefinition> columns = colRepo.accept(queryVisitor, tableName);
		int colOrder=0;

		// Build workflow columns DTO from source columns
		List<SILWorkflowSourceColumn> workflowSourceColumnList = columns.stream()//
				.filter(col -> allCols.stream().anyMatch(allColsColumn -> allColsColumn.equals(col.getColumnName())))
				.map(column -> {

//					if (targetCols.stream().anyMatch(col -> column.getColumnName().equals(col))) {
//						column.
//					}
//
//					if (pguidCols.stream().anyMatch(col -> column.getColumnName().equals(col))) {
//						column.setPguidFlag(true);
//					}
//
//					if (buidCols.stream().anyMatch(col -> column.getColumnName().equals(col))) {
//						column.setBuidFlag(true);
//					}
//
//					if (column.getColumnName().equals(changeCaptureCol)) {
//						column.setCcFlag(true);
//					}

					SILWorkflowSourceColumn wfCol = SILWorkflowSourceColumn.builder()//
							.autoColumn(column.)
							.integrationIdColumn(column.getIntegrationIdFlag())//
							.buidColumn(column.getBuidFlag()).pguidColumn(column.getPguidFlag())
							.changeCaptureColumn(column.getCcFlag())//
							.sourceColumnName(column.getColumnName())//
							.build();

					return wfCol;
				}).collect(Collectors.toList());

		// Build definition to be passed as input to generator
		WorkflowGeneratorInput = SILWorkflow.builder()//
				.columns(workflowSourceColumnList)
				.workflowUri("/GeneratedWorkflows/Repl/" + "SIL_" + tableName + "_" + loadType + ".xml")
				.workflowName("SIL_"  + tableName + "_" + loadType)//
				.loadType(loadType)
				.stageName(stageName)
				.tableName(tableName)
				.build();

		return WorkflowGeneratorInput;

	}

}
