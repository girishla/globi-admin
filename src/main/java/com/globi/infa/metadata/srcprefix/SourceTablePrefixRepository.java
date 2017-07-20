package com.globi.infa.metadata.srcprefix;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Repository to access {@link SourceTablePrefix} instances.
 * 
 * @author Girish lakshmanan
 */
@RepositoryRestResource(collectionResourceRel = "sourceprefixes",path = "sourceprefixes")
public interface SourceTablePrefixRepository extends CrudRepository<SourceTablePrefix, Long> {

	Optional<SourceTablePrefix> findByTableUniqueName(@Param("tableUniqueName") String tableUniqueName);


}
