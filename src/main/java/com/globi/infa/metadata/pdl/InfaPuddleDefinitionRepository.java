package com.globi.infa.metadata.pdl;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource(collectionResourceRel = "puddles",path = "puddles")
public interface InfaPuddleDefinitionRepository extends CrudRepository<InfaPuddleDefinition, Long>{
	
	
	
}