package com.globi.infa.sourcedefinition;



import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Repository to access {@link InfaSourceDefinition} instances.
 * 
 * @author Girish lakshmanan
 */
@RepositoryRestResource
public interface InfaSourceDefinitionRepository extends CrudRepository<InfaSourceDefinition, Long>, InfaSourceDefinitionCreator<SourceDefinitionInput> {
	
	/**
	 * Returns all {@link InfaSourceDefinition}s with the given {@link sourceTableName}.
	 * 
	 * @param sourceTableName must not be {@literal null}.
	 * @return
	 */
	Optional<InfaSourceDefinition> findBySourceTableName(@Param("sourceTableName") String sourceTableName);

	

}
