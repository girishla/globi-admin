package com.globi.infa.metadata.src;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource
public interface InfaSourceDefinitionRepository extends CrudRepository<InfaSourceDefinition, Long>{
	
}