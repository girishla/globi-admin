package com.globi.metadata.measures;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Repository to access {@link Measure} instances.
 * 
 * @author Girish lakshmanan
 */
@RepositoryRestResource(collectionResourceRel = "measures",path = "measures")
public interface MeasureRepository extends CrudRepository<Measure, Long> {

	
	/**
	 * Returns all {@link Measure}s with the given {@link type}.
	 * 
	 * @param type must not be {@literal null}.
	 * @return
	 */
	Optional<List<Measure>> findByType(@Param("type") String type);


	
}
