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
import com.globi.infa.workflow.InfaPTPWorkflowRepository;
import com.globi.infa.workflow.InfaWorkflowRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Profile("prod")
public class RepositoryLoaderEventListener implements WorkflowCreatedEventListener, RepositoryLoader {

	@Autowired
	WorkflowRunner pmcmdRunner;
	@Autowired
	InfaWorkflowRepository wfRepo;

	@Autowired
	private WorkflowMessageNotifier notifier;

	@Value("${git.dir}")
	private String gitDirectory;

	@Value("${pmrep.dir}")
	private String pmrepDirectory;

	@Value("${infa.reposervice}")
	private String infaRepositoryService;

	@Value("${infa.domain}")
	private String infaDomain;

	@Value("${infa.user}")
	private String infaUser;

	@Value("${infa.pwd}")
	private String infaPwd;

	private final Map<String, GeneratedWorkflow> workflows = new HashMap<>();

	@Override
	public void loadWorkflow(InfaPowermartObject generatedObject, GeneratedWorkflow wf) {
		try {
			ProcessResult outputConnectResult = new ProcessExecutor()//
					.command(pmrepDirectory + "pmrep", "connect", "-r", infaRepositoryService, "-d", infaDomain, "-n",
							infaUser, "-x", infaPwd)
					.readOutput(true).execute();
			
			String outputConnect=outputConnectResult.outputUTF8();
			
			
			notifier.message(wf, outputConnect);
			
			if (!outputConnect.contains("Connected")) {
				throw new InvalidExitValueException("Could not connect to Powercenter Repository." + "\n" + outputConnect, outputConnectResult);
			}


			ProcessResult result = new ProcessExecutor()//
					.command(pmrepDirectory + "pmrep", "ObjectImport", "-i", gitDirectory + "\\" + generatedObject.pmObjectName + ".xml",
							"-c", gitDirectory + "\\infagen\\infacontrol_" + generatedObject.folderName + ".xml")
					.readOutput(true).execute();

			String output = result.outputUTF8();
			
			
			notifier.message(wf, output);

			log.info("*************************************");
			log.info("*************************************");
			log.info(outputConnect);
			log.info("*************************************");
			log.info(output);
			log.info("*************************************");
			
			
			
			
			if ((!output.contains("0 Errors")) || output.contains("Failed to execute ObjectImport")) {
				throw new InvalidExitValueException("Errors during upload." + "\n" + output, result);
			}

			pmcmdRunner.run(generatedObject, wf);
			
			

		} catch (InvalidExitValueException | IOException | InterruptedException | TimeoutException e1) {

			// update wf status column to error

			e1.printStackTrace();
			
			notifier.message(wf, e1.getMessage());
			
			throw new WorkflowGenerationException(wf,"Unable to upload Workflow!" + "\n" + e1.getMessage());
		}

	}

	@Override
	public void notify(InfaPowermartObject generatedObject, GeneratedWorkflow wf) {

		this.workflows.put(generatedObject.pmObjectName, wf);
		

	}

}
