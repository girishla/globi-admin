package com.globi.infa.generator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.workflow.GeneratedWorkflow;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PmcmdRunnerEventListener implements WorkflowCreatedEventListener {

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
	public void notify(InfaPowermartObject generatedObject, GeneratedWorkflow wf) {

		this.workflows.put(generatedObject.pmObjectName, wf);

		try {

			ProcessResult result = new ProcessExecutor()//
					.command(pmcmdDirectory + "pmcmd", "startworkflow", "-sv", infaIntegrationService,"-d",infaDomain,"-u",infaUser,"-p",infaPwd,"-f", generatedObject.folderName,generatedObject.pmObjectName) 
					.readOutput(true).execute();

			String output = result.outputUTF8();

			log.info("*************************************");
			log.info("*************************************");
			log.info(output);
			log.info("*************************************");			
/*			if (!output.contains("0 Errors")) {
				throw new InvalidExitValueException("Errors during upload. Please see logs for more info.",
						result);
			}

			*/


		} catch (InvalidExitValueException | IOException | InterruptedException | TimeoutException e1) {

			
			//update wf to error
			
			e1.printStackTrace();
		}

	}

}
