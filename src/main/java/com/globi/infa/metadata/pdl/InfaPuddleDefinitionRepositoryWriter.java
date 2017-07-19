package com.globi.infa.metadata.pdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.globi.infa.datasource.core.InfaTargetToOracleDataTypeMapper;
import com.globi.infa.generator.RepositoryLoader;
import com.globi.infa.generator.WorkflowCreatedEventListener;
import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.generator.builder.InfaTargetObject;
import com.globi.infa.workflow.GeneratedWorkflow;

import lombok.extern.slf4j.Slf4j;
import xjc.TARGETFIELD;

@Component
@Slf4j
public class InfaPuddleDefinitionRepositoryWriter implements WorkflowCreatedEventListener {

	List<InfaTargetObject> targets;

	@Autowired
	private InfaPuddleDefinitionRepository tgtRepo;

	@Autowired
	private InfaTargetToOracleDataTypeMapper mapper;

	@Autowired
	private PuddleDDLRepository ddlGen;
	
	@Autowired
	private RepositoryLoader repoLoader;

	public InfaPuddleDefinitionRepositoryWriter(InfaPuddleDefinitionRepository tgtRepo,
			InfaTargetToOracleDataTypeMapper mapper) {

		this.tgtRepo = tgtRepo;
		this.mapper = mapper;

	}

	private InfaPuddleColumnDefinition getTargetCol(TARGETFIELD field, int index) {

		InfaPuddleColumnDefinition col = InfaPuddleColumnDefinition.builder()//
				.columnDataType(mapper.mapType(field.getDATATYPE()))//
				.columnName(field.getNAME())//
				.columnNumber(index)//
				.nullable(field.getNULLABLE()).precision(Integer.parseInt(field.getPRECISION()))
				.scale(Integer.parseInt(field.getSCALE())).build();
		return col;

	}

	private void writeTarget(InfaTargetObject target) {

		InfaPuddleDefinition tgt = InfaPuddleDefinition.builder()//
				.pdlTableName(target.getName())//
				.ownerName("PDL")//
				.build();

		List<InfaPuddleColumnDefinition> targetCols = new ArrayList<>();
		int[] idx = { 1 };

		// Do system columns first
		for (TARGETFIELD field : target.getTarget().getTARGETFIELD().stream()
				.filter(col -> col.getNAME().startsWith("SYS_")).collect(Collectors.toList())) {

			targetCols.add(getTargetCol(field, idx[0]++));

		}

		// Do everything else
		for (TARGETFIELD field : target.getTarget().getTARGETFIELD().stream()
				.filter(col -> !(col.getNAME().startsWith("SYS_"))).collect(Collectors.toList())) {

			targetCols.add(getTargetCol(field, idx[0]++));

		}

		tgt.setColumns(targetCols);

		Optional<InfaPuddleDefinition> existingPuddleDefn = tgtRepo.findByPdlTableName(target.getName());
		if (existingPuddleDefn.isPresent()) {
			existingPuddleDefn.get().getColumns().clear();
			InfaPuddleDefinition cleanedPuddle = tgtRepo.save(existingPuddleDefn.get());
			tgt.setId(cleanedPuddle.getId());
			tgt.setVersion(cleanedPuddle.getVersion());
		}

		tgtRepo.save(tgt);

	}


	
	public void writeToRepository(InfaPowermartObject pmObj) {

		this.targets = pmObj.folderObjects.stream()//
				.filter(fo -> fo.getType().equals("TARGET"))//
				.map(fo -> (InfaTargetObject) fo).collect(Collectors.toList());

		this.targets.forEach(target -> {
			this.writeTarget(target);
			log.info(String.format("Generating DDL for table %s", target.getName()));
			ddlGen.generateDDL("NoRelease", target.getName(), "Y", "N");
			

		});

	}

	public void writeToRepository(List<InfaTargetObject> targets) {

		this.targets = targets;
		this.targets.forEach(target -> {
			this.writeTarget(target);
			log.info(String.format("Generating DDL for table %s", target.getName()));
			ddlGen.generateDDL("NoRelease", target.getName(), "Y", "N");
		});

	}

	@Override
	public void notify(InfaPowermartObject generatedObject, GeneratedWorkflow wf) {

		this.writeToRepository(generatedObject);
		repoLoader.loadWorkflow(generatedObject, wf);

	}

}
