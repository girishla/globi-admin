package com.globi.infa.generator;

import static com.globi.infa.generator.builder.InfaObjectMother.getFolderFor;
import static com.globi.infa.generator.builder.InfaObjectMother.getRepository;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import lombok.extern.slf4j.Slf4j;
import xjc.FOLDER;
import xjc.REPOSITORY;

@Component
@Slf4j
public class AggregateGitWriterEventListener
		implements WorkflowCreatedEventListener, WorkflowBatchRequestEventListener {

	@Value("${git.dir}")
	private String gitDirectory;
	@Autowired
	private Jaxb2Marshaller marshaller;

	private final Map<String, InfaPowermartObject> generatedObjects = new HashMap<>();
	private final Map<String, GeneratedWorkflow> workflows = new HashMap<>();
	private final Set<String> folderSet = new HashSet<String>();

	@Override
	public void notify(InfaPowermartObject generatedObject, GeneratedWorkflow wf) {

		this.generatedObjects.put(generatedObject.folderName + "::" + generatedObject.pmObjectName, generatedObject);
		this.workflows.put(generatedObject.pmObjectName, wf);

		if (!folderSet.contains(generatedObject.folderName)) {
			folderSet.add(generatedObject.folderName);
			
		}

	}

	private InfaPowermartObject getAggregateForAllFolders() {

		List<InfaPowermartObject> aggregateFolderPowermartObjects = new ArrayList<>();

		folderSet.forEach(folder->{

			List<InfaFolderObject> folderObjList = this.generatedObjects.entrySet().stream()//
					.filter(pmObjectEntry -> pmObjectEntry.getKey().startsWith(folder))
					.map(pmObjectEntry -> pmObjectEntry.getValue().folderObjects)//
					.flatMap(List::stream)//
					.collect(Collectors.toList());

			aggregateFolderPowermartObjects.add(getAggregatePowermartObject(folder, folderObjList));

		});

		InfaPowermartObject pmObj = PowermartObjectBuilder//
				.newBuilder()//
				.powermartObject()//
				.repository(getRepository())//
				.buildPowermartObjWithNoFolders();
		
				
		aggregateFolderPowermartObjects.forEach(pmo->{
			
			getFolderList(pmObj).addAll(getFolderList(pmo));
			
		
		});
		
		return pmObj;
	}

	private List<FOLDER> getFolderList(InfaPowermartObject pmObj) {

		List<FOLDER> folders = null;

		if (pmObj != null) {
			for (REPOSITORY repository : pmObj.pmObject.getREPOSITORY()) {

				folders = repository.getFOLDER();

			}
		}
		return folders;

	}

	private InfaPowermartObject getAggregatePowermartObject(String folderName,
			List<InfaFolderObject> folderObjInputList) {

		List<InfaFolderObject> folderObjUniqueList;

		folderObjUniqueList = folderObjInputList.stream().collect(
				collectingAndThen(toCollection(() -> new TreeSet<>(comparing(InfaFolderObject::getUniqueName))),
						ArrayList<InfaFolderObject>::new));

		InfaPowermartObject pmObj = PowermartObjectBuilder//
				.newBuilder()//
				.powermartObject()//
				.repository(getRepository())//
				.folder(getFolderFor(folderName, "Pull to puddle folder"))//
				.buildPowermartObjWithBlankFolder();

		List<Object> folderChildren = folderObjUniqueList.stream()//
				.map(folderChild -> folderChild.getFolderObj())//
				.collect(Collectors.toList());

		pmObj.pmObject.getREPOSITORY().forEach(repo -> {
			repo.getFOLDER().forEach(folder -> addChildren(folder, folderChildren));
		});

		return pmObj;

	}

	private FOLDER addChildren(FOLDER folder, List<Object> children) {

		folder.getFOLDERVERSIONOrCONFIGOrSCHEDULEROrTASKOrSESSIONOrWORKLETOrWORKFLOWOrSOURCEOrTARGETOrTRANSFORMATIONOrMAPPLETOrMAPPINGOrSHORTCUTOrEXPRMACRO()
				.addAll(children);

		return folder;

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
		String fileName = "AGGREGATED_WF";

		try (Repository repository = builder.setGitDir(repoDir)//
				.readEnvironment().findGitDir() // scan up the file system tree
				.build()) {

			try (Git git = Git.open(new File(gitDirectory + ".git"));) {

				this.saveXML(this.getAggregateForAllFolders().pmObject, fileName);

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
