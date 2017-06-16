package com.globi.infa.generator;



import static java.util.stream.Collectors.collectingAndThen;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;
import static com.globi.infa.generator.builder.InfaObjectMother.getFolderFor;
import static com.globi.infa.generator.builder.InfaObjectMother.getRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
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
public class AggregateGitWriterEventListener
		implements WorkflowCreatedEventListener, WorkflowBatchRequestEventListener {

	@Value("${git.dir}")
	private String gitDirectory;
	@Autowired
	private Jaxb2Marshaller marshaller;

	private final Map<String, InfaPowermartObject> generatedObjects = new HashMap<>();
	private final Map<String, GeneratedWorkflow> workflows = new HashMap<>();

	@Override
	public void notify(InfaPowermartObject generatedObject, GeneratedWorkflow wf) {

		this.generatedObjects.put(generatedObject.pmObjectName, generatedObject);
		this.workflows.put(generatedObject.pmObjectName, wf);

	}

	private InfaPowermartObject getAggregatePowermartObject() {

//		TreeSet<InfaFolderObject> folderObjectSet = new TreeSet<>();

		List<InfaFolderObject> folderObjList =this.generatedObjects.entrySet().stream()//
				.map(pmObjectEntry -> {

					return pmObjectEntry.getValue().folderObjects;

				}).flatMap(List::stream)//
				 .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparing(InfaFolderObject::getUniqueName))),
                         ArrayList<InfaFolderObject>::new));
//				.forEach(folderObject -> folderObjectSet.add(folderObject));
		
//		List<InfaFolderObject> folderObjList = new ArrayList<>(folderObjectSet);

		InfaPowermartObject pmObj = PowermartObjectBuilder//
				.newBuilder()//
				.powermartObject()//
				.repository(getRepository())//
				.folder(getFolderFor("DUMMY", "Pull to puddle folder"))//
				.buildPowermartObjWithBlankFolder();

		List<Object> folderChildren = folderObjList.stream()//
				.map(folderChild -> folderChild.getFolderObj())//
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
		String fileName = "AGGREGATED_WF_" + UUID.randomUUID().toString();

		try (Repository repository = builder.setGitDir(repoDir)//
				.readEnvironment().findGitDir() // scan up the file system tree
				.build()) {

			try (Git git = Git.open(new File(gitDirectory + ".git"));) {

				this.saveXML(this.getAggregatePowermartObject().pmObject, fileName);

				git.add()//
						.addFilepattern(fileName + ".xml").call();

				git.commit().setMessage("Added " + fileName + ".xml").call();

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
		generatedObjects.clear();
		workflows.clear();

	}

}
