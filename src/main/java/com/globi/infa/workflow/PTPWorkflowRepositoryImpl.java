package com.globi.infa.workflow;

import org.springframework.beans.factory.annotation.Autowired;

import com.globi.infa.DataSourceTableDTO;

public class PTPWorkflowRepositoryImpl implements InfaWorkflowCreator<PTPWorkflow> {

	@Autowired
	PTPWorkflowRepository wfRepository;

	@Override
	public PTPWorkflow createWorkflow(PTPWorkflow ptpWorkflow) {

		

		
		
		
		
		
		
		return wfRepository.save(ptpWorkflow);

	}

}
