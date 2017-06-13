package com.globi.infa.metadata.pdl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.globi.infa.datasource.core.InfaTargetToOracleDataTypeMapper;
import com.globi.infa.generator.WorkflowCreatedEventListener;
import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.generator.builder.InfaTargetObject;
import com.globi.infa.workflow.GeneratedWorkflow;

import xjc.TARGETFIELD;

@Component
public class InfaPuddleDefinitionRepositoryWriter implements WorkflowCreatedEventListener {

	List<InfaTargetObject> targets;

	@Autowired
	private InfaPuddleDefinitionRepository tgtRepo;

	@Autowired
	private InfaTargetToOracleDataTypeMapper mapper;

	public InfaPuddleDefinitionRepositoryWriter(InfaPuddleDefinitionRepository tgtRepo,
			InfaTargetToOracleDataTypeMapper mapper) {

		this.tgtRepo = tgtRepo;
		this.mapper = mapper;

	}

	private void writeTarget(InfaTargetObject target) {

		InfaPuddleDefinition tgt = InfaPuddleDefinition.builder()//
				.pdlTableName(target.getName())//
				.ownerName("PDL")//
				.build();

		List<InfaPuddleColumnDefinition> targetCols = new ArrayList<>();
		int[] idx = { 1 };

		for (TARGETFIELD field : target.getTarget().getTARGETFIELD()) {

			InfaPuddleColumnDefinition col = InfaPuddleColumnDefinition.builder()//
					.columnDataType(mapper.mapType(field.getDATATYPE()))//
					.columnName(field.getNAME())//
					.columnNumber(idx[0]++)//
					.nullable(field.getNULLABLE()).precision(Integer.parseInt(field.getPRECISION()))
					.scale(Integer.parseInt(field.getSCALE())).build();

			targetCols.add(col);

		}

		tgt.setColumns(targetCols);

		tgtRepo.save(tgt);

	}

	public void writeToRepository(InfaPowermartObject pmObj) {

		this.targets = pmObj.folderObjects.stream()//
				.filter(fo -> fo.getType().equals("TARGET"))//
				.map(fo -> (InfaTargetObject) fo).collect(Collectors.toList());

		this.targets.forEach(target -> this.writeTarget(target));

	}

	public void writeToRepository(List<InfaTargetObject> targets) {

		this.targets = targets;
		this.targets.forEach(target -> this.writeTarget(target));

	}

	@Override
	public void notify(InfaPowermartObject generatedObject, GeneratedWorkflow wf) {

		this.writeToRepository(generatedObject);

	}

}
