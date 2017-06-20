package com.globi.infa.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.workflow.GeneratedWorkflow;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AggregatePTPPmcmdFileWriterEventListener
		implements WorkflowCreatedEventListener, WorkflowBatchRequestEventListener {

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
	private final String pmcmdPattern = "call {{pmcmdDirectory}}\\pmcmd   startworkflow -sv {{infaIntegrationService}} -d {{infaDomain}} -u {{infaUser}} -p {{infaPwd}} -f {{folder}} {{wfName}}";
	private final Map<String, String> interpolationValues = new HashMap<>();
	private String appendedExtractCommand="";
	private String appendedPrimaryCommand="";

	@Override
	public void notify(InfaPowermartObject generatedObject, GeneratedWorkflow wf) {

		this.workflows.put(generatedObject.pmObjectName, wf);

		interpolationValues.put("pmcmdDirectory", pmcmdDirectory);
		interpolationValues.put("infaIntegrationService", infaIntegrationService);
		interpolationValues.put("infaDomain", infaDomain);
		interpolationValues.put("infaUser", infaUser);
		interpolationValues.put("infaPwd", infaPwd);
		interpolationValues.put("folder", generatedObject.folderName);
		interpolationValues.put("wfName", generatedObject.pmObjectName);

		if (generatedObject.pmObjectName.endsWith("Extract")) {
			appendedExtractCommand += "\n" + new StrSubstitutor(interpolationValues, "{{", "}}").replace(pmcmdPattern);

		} else {
			if (generatedObject.pmObjectName.endsWith("Primary")) {
				appendedPrimaryCommand += "\n"
						+ new StrSubstitutor(interpolationValues, "{{", "}}").replace(pmcmdPattern);

			}

		}

	}

	private void saveXML(String command, String fileName) throws IOException {
		FileOutputStream os = null;
		PrintStream printStream = null;
		try {
			os = new FileOutputStream(gitDirectory + fileName);
			printStream=new PrintStream(os);
			printStream.print(command);
			printStream.close();
		} finally {
			if (os != null) {
				os.close();
				printStream.close();
			}
		}
	}

	private void writeToGit() throws IOException {

		File repoDir = new File(gitDirectory);
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		String fileNameExtract = "runWorkflowsExtract.cmd";
		String fileNamePrimary = "runWorkflowsPrimary.cmd";

		try (Repository repository = builder.setGitDir(repoDir)//
				.readEnvironment().findGitDir() // scan up the file system tree
				.build()) {

			try (Git git = Git.open(new File(gitDirectory + ".git"));) {

				this.saveXML(appendedExtractCommand, fileNameExtract);
				this.saveXML(appendedPrimaryCommand, fileNamePrimary);
				
				git.add()//
						.addFilepattern(fileNameExtract).call();
				git.add()//
				.addFilepattern(fileNamePrimary).call();

				git.commit().setMessage("Added execution commands").call();

			} catch (NoFilepatternException e) {
				e.printStackTrace();
			} catch (GitAPIException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void notifyBatchComplete() {

		try {
			this.writeToGit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void notifyBatchStart() {
		workflows.clear();

	}

}
