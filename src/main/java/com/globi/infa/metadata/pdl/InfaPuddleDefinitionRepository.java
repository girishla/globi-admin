package com.globi.infa.metadata.pdl;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource
public interface InfaPuddleDefinitionRepository extends CrudRepository<InfaPuddleDefinition, Long>{
	
	
	
}