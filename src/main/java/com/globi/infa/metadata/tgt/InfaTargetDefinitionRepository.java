package com.globi.infa.metadata.tgt;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource
public interface InfaTargetDefinitionRepository extends CrudRepository<InfaTargetDefinition, Long>{
	
	
	
}