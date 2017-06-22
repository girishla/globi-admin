package com.globi.infa.generator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.workflow.GeneratedWorkflow;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Profile("test")
public class DummyLoaderEventListener implements WorkflowCreatedEventListener, PmrepLoader {

	@Autowired
	PmcmdRunner pmcmdRunner;


	@Override
	public void loadWorkflow(String folderName, String objectName) {

		log.info("*************************************");
		log.info("I promise to load the workflow when it is the correct profile");
		log.info("*************************************");

		pmcmdRunner.run(folderName, objectName);

	}

	@Override
	public void notify(InfaPowermartObject generatedObject, GeneratedWorkflow wf) {

		// Do nothing
	}

}
