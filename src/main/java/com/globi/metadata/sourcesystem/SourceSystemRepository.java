package com.globi.metadata.sourcesystem;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Repository to access {@link SourceSystem} instances.
 * 
 * @author Girish lakshmanan
 */
@RepositoryRestResource(collectionResourceRel = "sourcesystems",path = "sourcesystems")
//@PreAuthorize("@securityService.hasProtectedAccess()")

//@PreAuthorize("hasAuthority('USER')")
public interface SourceSystemRepository extends CrudRepository<SourceSystem, Long> {

	
	/**
	 * Returns all {@link SourceSystem}s with the given {@link Name}.
	 * 
	 * @param name must not be {@literal null}.
	 * @return
	 */
	Optional<SourceSystem> findByName(@Param("name") String name);


}
