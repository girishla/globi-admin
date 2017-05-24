package com.globi.infa.sourcedefinition;




import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.globi.metadata.sourcesystem.SourceSystem;
import com.globi.metadata.sourcesystem.SourceSystemRepository;

public class InfaSourceDefinitionRepositoryImpl implements InfaSourceDefinitionCreator<SourceDefinitionInput> {

	@Autowired
	InfaSourceDefinitionRepository sourceDefinitionRepository;
	
	@Autowired
	private SourceSystemRepository sourceSystemRepo;

	
	
	@Override
	public InfaSourceDefinition createSourceDefiniton(SourceDefinitionInput sourceDefinitionInput) {
		
		Optional<SourceSystem> sourceSystem=sourceSystemRepo.findByName(sourceDefinitionInput.getSourceName());

		
		return sourceDefinitionRepository.save(InfaSourceDefinition.builder()//
				.sourceTableName("REPL_" + sourceDefinitionInput.getSourceTableName())//
				.databaseName(sourceSystem.get().getDbName())
				.databaseType(sourceSystem.get().getDbType())
				.ownerName(sourceSystem.get().getOwnerName())
				.build());
				
		
		
	}

}
