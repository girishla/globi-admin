package com.globi.infa.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.stream.StreamResult;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;

import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.workflow.GeneratedWorkflow;

@Component
public class GitWriterEventListener implements WorkflowCreatedEventListener {

	@Value("${git.dir}")
	private String gitDirectory;
	@Autowired
	private Jaxb2Marshaller marshaller;

	private InfaPowermartObject generatedObject;
	private GeneratedWorkflow wf;

	@Override
	public void notify(InfaPowermartObject generatedObject, GeneratedWorkflow wf) {

		this.generatedObject = generatedObject;
		this.wf = wf;

		try {
			this.writeToGit();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void saveXML(Object jaxbObject, String fileName) throws IOException {
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(gitDirectory + fileName + ".xml");
			this.marshaller.marshal(jaxbObject, new StreamResult(os));
		} finally {
			if (os != null) {
				os.close();
			}
		}
	}

	private void writeToGit() throws IOException {

		File repoDir = new File(gitDirectory);
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try (Repository repository = builder.setGitDir(repoDir)//
				.readEnvironment().findGitDir() // scan up the file system tree
				.build()) {

			try (Git git = Git.open(new File(gitDirectory + ".git"));) {

				this.saveXML(this.generatedObject.pmObject, this.generatedObject.pmObjectName);

				git.add()//
						.addFilepattern(this.generatedObject.pmObjectName+ ".xml").call();

				git.commit().setMessage("Added " + this.generatedObject.pmObjectName).call();

			} catch (NoFilepatternException e) {
				e.printStackTrace();
			} catch (GitAPIException e) {
				e.printStackTrace();
			}

		}

	}

}
