package com.globi.infa.generator;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.workflow.GeneratedWorkflow;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Profile("test")
public class DummyRunnerEventListener implements WorkflowCreatedEventListener, WorkflowRunner {


	@Override
	public void run(InfaPowermartObject generatedObject, GeneratedWorkflow wf) {

		log.info("*************************************");
		log.info("I promise to run this workflow if it is the correct profile");
		log.info("*************************************");
	}

	@Override
	public void notify(InfaPowermartObject generatedObject, GeneratedWorkflow wf) {

		// Do nothing

	}

}
