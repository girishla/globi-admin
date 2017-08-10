package com.globi.infa.generator.sil;

import java.util.List;
import java.util.stream.Collectors;

import com.globi.infa.metadata.sil.SilMetadata;
import com.globi.infa.workflow.sil.SILWorkflow;
import com.globi.infa.workflow.sil.SILWorkflowSourceColumn;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetadataToSILWorkflowDefnConverter {

	private final List<SilMetadata> columns;
	private final String tableName;

	public MetadataToSILWorkflowDefnConverter(String tableName,List<SilMetadata> columns) {
		this.columns = columns;
		this.tableName=tableName;
	}

	public SILWorkflow  getSilWorkflowDefinition() {

		List<SILWorkflowSourceColumn> cols= columns.stream()
				.filter(col -> col.isStageColumnFlag() && (col.getColumnType().equals("Foreign Key")
						|| col.getColumnType().equals("Measure Attribute") || col.getColumnType().equals("Measure")
						|| col.getColumnName().equals("DATASOURCE_NUM_ID")))
				.map(col -> {
					return SILWorkflowSourceColumn.builder()//
							.columnName(col.getColumnName())//
							.autoColumn(col.getAutoColumnFlag())//
							.columnType(col.getColumnType())//
							.dimTableName(col.getDimTableName())//
							.columnOrder(Integer.parseInt(col.getColumnOrder()))
							.domainLookupColumn(col.getDomainLookupColumnFlag())//
							.legacyColumn(col.getLegacyColumnFlag())//
							.miniDimColumn(col.getMiniDimColumnFlag())//
							.targetColumn(col.getTargetColumnFlag())//
							.build();
				}).collect(Collectors.toList());

		
		return SILWorkflow.builder()//
				.columns(cols)//
				.loadType("Fact")//
				.stageName("X_" + tableName)//
				.tableName(tableName)//
				.workflowName("SIL_" +  tableName + "_Fact")//
				.workflowStatus("Queued")//
				.workflowUri("")//
				.build();
		

	}

}
