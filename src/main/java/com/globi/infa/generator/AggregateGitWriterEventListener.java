package com.globi.infa.generator;

import static com.globi.infa.generator.builder.InfaObjectStaticFactory.getFolderFor;
import static com.globi.infa.generator.builder.InfaObjectStaticFactory.getRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.transform.stream.StreamResult;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;

import com.globi.infa.generator.builder.InfaFolderObject;
import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.generator.builder.PowermartObjectBuilder;
import com.globi.infa.workflow.GeneratedWorkflow;

@Component
public class AggregateGitWriterEventListener implements WorkflowCreatedEventListener {

	@Value("${git.dir}")
	private String gitDirectory;
	@Autowired
	private Jaxb2Marshaller marshaller;

	private final Map<String, InfaPowermartObject> generatedObjects = new HashMap<>();
	private final Map<String, GeneratedWorkflow> workflows = new HashMap<>();

	@Override
	public void notify(InfaPowermartObject generatedObject, GeneratedWorkflow wf) {

		this.generatedObjects.put(wf.getWorkflow().getWorkflowName(), generatedObject);
		this.workflows.put(wf.getWorkflow().getWorkflowName(), wf);

		try {
			this.writeToGit();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private InfaPowermartObject getAggregatePowermartObject() {

		HashSet<InfaFolderObject> folderObjectSet = new HashSet<>();

		this.generatedObjects.entrySet().stream()//
				.map(pmObjectEntry -> {

					return pmObjectEntry.getValue().folderObjects;

				}).flatMap(List::stream)//
				.forEach(folderObject -> folderObjectSet.add(folderObject));

		List<InfaFolderObject> folderObjList = new ArrayList<>(folderObjectSet);

		InfaPowermartObject pmObj = PowermartObjectBuilder//
				.newBuilder()//
				.powermartObject().repository(getRepository())//
				.folder(getFolderFor("DUMMY", "Pull to puddle folder"))//
				.buildPowermartObjWithBlankFolder();
		
		List<Object> folderChildren=folderObjList.stream()//
				.map(folderChild->folderChild.getFolderObj())//
				.collect(Collectors.toList());

		pmObj.pmObject.getREPOSITORY().forEach(repo -> {
			repo.getFOLDER()
					.forEach(folder -> folder
							.getFOLDERVERSIONOrCONFIGOrSCHEDULEROrTASKOrSESSIONOrWORKLETOrWORKFLOWOrSOURCEOrTARGETOrTRANSFORMATIONOrMAPPLETOrMAPPINGOrSHORTCUTOrEXPRMACRO()
							.addAll(folderChildren));
		});
		
		return pmObj;

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

				this.saveXML(this.getAggregatePowermartObject().pmObject, "AGGREGATED_WF");

				git.add()//
						.addFilepattern( "AGGREGATED_WF.xml").call();

				git.commit().setMessage("Added AGGREGATED_WF.xml").call();
				System.out.println("Added file AGGREGATED_WF.xml" + " to repository at "
						+ repository.getDirectory());
			} catch (NoFilepatternException e) {
				e.printStackTrace();
			} catch (GitAPIException e) {
				e.printStackTrace();
			}

		}

	}

}
