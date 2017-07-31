package com.globi.infa.generator;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.workflow.PTPWorkflowSourceColumn;

public abstract class AbstractMappingGenerator {
	
	
	protected List<InfaSourceColumnDefinition> getFilteredSourceDefnColumns(
			List<InfaSourceColumnDefinition> allTableColumns, List<PTPWorkflowSourceColumn> inputSelectedColumns) {

		Map<String, InfaSourceColumnDefinition> allColsMap = allTableColumns.stream()
				.collect(Collectors.toMap(InfaSourceColumnDefinition::getColumnName, Function.identity()));

		inputSelectedColumns.stream().forEach(inputColumn -> {
			if (allColsMap.containsKey(inputColumn.getSourceColumnName())) {
				allColsMap.get(inputColumn.getSourceColumnName())
						.setIntegrationIdFlag(inputColumn.isIntegrationIdColumn());
				allColsMap.get(inputColumn.getSourceColumnName()).setColumnSequence(inputColumn.getColumnSequence());
				allColsMap.get(inputColumn.getSourceColumnName()).setBuidFlag(inputColumn.isBuidColumn());
				allColsMap.get(inputColumn.getSourceColumnName()).setCcFlag(inputColumn.isChangeCaptureColumn());
				allColsMap.get(inputColumn.getSourceColumnName()).setPguidFlag(inputColumn.isPguidColumn());
				allColsMap.get(inputColumn.getSourceColumnName()).setSelected(true);
			}
		});

		List<InfaSourceColumnDefinition> matchedColumns = allColsMap.values()//
				.stream()//
				.filter(column -> column.getSelected()).collect(Collectors.toList());

		return matchedColumns;

	}


}
