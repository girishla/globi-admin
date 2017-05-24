package com.globi.infa.workflow;

import org.springframework.beans.factory.annotation.Autowired;

import com.globi.infa.DataSourceTable;

public class InfaWorkflowRepositoryImpl implements InfaWorkflowCreator<DataSourceTable> {

	@Autowired
	InfaWorkflowRepository wfRepository;

	@Override
	public InfaWorkflow createWorkflow(DataSourceTable dataSourceTable) {

		return wfRepository.save(InfaWorkflow.builder()//
				.workflowName("REPL_" + dataSourceTable.getTableName())//
				.workflowScmUri("/GeneratedWorkflows/Repl/" + "REPL_" + dataSourceTable.getTableName() + ".xml")//
				.build());

	}

}
