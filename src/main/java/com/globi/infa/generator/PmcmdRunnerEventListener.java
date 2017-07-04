package com.globi.infa.generator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.workflow.GeneratedWorkflow;
import com.globi.infa.workflow.InfaWorkflowRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Profile("prod")
public class PmcmdRunnerEventListener implements WorkflowCreatedEventListener, WorkflowRunner {

	@Autowired
	InfaWorkflowRepository wfRepo;
	
	@Value("${git.dir}")
	private String gitDirectory;

	@Value("${pmcmd.dir}")
	private String pmcmdDirectory;

	@Value("${infa.intservice}")
	private String infaIntegrationService;

	@Value("${infa.domain}")
	private String infaDomain;

	@Value("${infa.user}")
	private String infaUser;

	@Value("${infa.pwd}")
	private String infaPwd;

	private final Map<String, GeneratedWorkflow> workflows = new HashMap<>();

	
	@Override
	public void run(InfaPowermartObject generatedObject, GeneratedWorkflow wf) {

		try {

			ProcessResult result = new ProcessExecutor()//
					.command(pmcmdDirectory + "pmcmd", "startworkflow", "-sv", infaIntegrationService, "-d", infaDomain,
							"-u", infaUser, "-p", infaPwd, "-f", generatedObject.folderName,
							generatedObject.pmObjectName)
					.readOutput(true).execute();

			String output = result.outputUTF8();

			log.info("*************************************");
			log.info("*************************************");
			log.info(output);

			if (!output.contains("started successfully")) {
				throw new InvalidExitValueException(
						"Errors while attempting to start workflow. Please see logs for more info.", result);
			}
			
			wf.getWorkflow().setWorkflowRunStatus("Started");
			wf=wfRepo.save(wf.getWorkflow());
			

		} catch (InvalidExitValueException | IOException | InterruptedException | TimeoutException e1) {

			// update wf to error
			e1.printStackTrace();
			
			wf.getWorkflow().setWorkflowRunStatus("Error");
			wf=wfRepo.save(wf.getWorkflow());
		}

	}

	@Override
	public void notify(InfaPowermartObject generatedObject, GeneratedWorkflow wf) {

		this.workflows.put(generatedObject.pmObjectName, wf);

		this.run(generatedObject,wf);

	}

}
