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
import com.globi.infa.notification.messages.WorkflowMessageNotifier;
import com.globi.infa.workflow.GeneratedWorkflow;
import com.globi.infa.workflow.InfaWorkflowRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Profile("prod")
public class PmcmdRunnerEventListener implements WorkflowCreatedEventListener, WorkflowRunner {

	@Autowired
	InfaWorkflowRepository wfRepo;
	
	@Autowired
	private WorkflowMessageNotifier notifier;
	
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
			
			
			notifier.message(wf, output);

			if (!output.contains("started successfully")) {
				throw new InvalidExitValueException(
						"Errors while attempting to start workflow. Please see logs for more info.", result);
			}
			


		} catch (InvalidExitValueException | IOException | InterruptedException | TimeoutException e1) {

			e1.printStackTrace();
			notifier.message(wf, e1.getMessage());
		
			throw new WorkflowGenerationException(wf,"Unable to Run Workflow!" + "\n" + e1.getMessage());

		}

	}

	@Override
	public void notify(InfaPowermartObject generatedObject, GeneratedWorkflow wf) {

		this.workflows.put(generatedObject.pmObjectName, wf);

		this.run(generatedObject,wf);

	}

}
