package com.globi.infa.metadata.src;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource(collectionResourceRel = "sourcetables",path = "sourcetables")
public interface InfaSourceDefinitionRepository extends CrudRepository<InfaSourceDefinition, Long>{
	
	Optional<InfaSourceDefinition> findBySourceTableUniqueName(@Param("sourcTableUniqueName") String sourceTableUniqueName);

	
	
}