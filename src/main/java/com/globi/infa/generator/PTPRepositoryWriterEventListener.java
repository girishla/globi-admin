package com.globi.infa.generator;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.workflow.GeneratedWorkflow;
import com.globi.infa.workflow.InfaWorkflow;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PTPRepositoryWriterEventListener implements WorkflowCreatedEventListener {

	@Autowired
	PTPWorkflowRepository wfRepository;

	

	private void  saveOrUpdateWFDefinition(GeneratedWorkflow wf){
		
		Optional<PTPWorkflow> queriedWorkflow = wfRepository
				.findByWorkflowName(wf.getWorkflow().getWorkflowName());

		PTPWorkflow inWorkflow = (PTPWorkflow) wf;
		PTPWorkflow wfDefinition;

		if (queriedWorkflow.isPresent()) {
			log.info("***********************************");
			log.info("found existing wf");

			//Only columns can change during updates
			wfDefinition = queriedWorkflow.get();
			wfDefinition.setColumns(inWorkflow.getColumns());

		} else {

			log.info("***********************************");
			log.info("did not find existing wf");
			
			wfDefinition = PTPWorkflow.builder()//
					.sourceName(inWorkflow.getSourceName())//
					.sourceTableName(inWorkflow.getSourceTableName()).columns(inWorkflow.getColumns())
					.workflowUri("/GeneratedWorkflows/Repl/" + "PTP_" + inWorkflow.getSourceName()+ "_"+ inWorkflow.getSourceTableName()  + ".xml")
					.workflowType("PTP")
					.workflowName("PTP_" + inWorkflow.getSourceName()+ "_"+ inWorkflow.getSourceTableName()  + "_Extract")
					.build();


		}
		
		wfRepository.save(wfDefinition);
	}
	
	
	@Override
	public void notify(InfaPowermartObject generatedObject, GeneratedWorkflow wf) {

		this.saveOrUpdateWFDefinition(wf);

	}

}
