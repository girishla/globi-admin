package com.globi.infa.metadata.src;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource(collectionResourceRel = "sources",path = "sources")
public interface InfaSourceDefinitionRepository extends CrudRepository<InfaSourceDefinition, Long>{
	
}