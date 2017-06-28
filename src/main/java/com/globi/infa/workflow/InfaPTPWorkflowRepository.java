package com.globi.infa.workflow;


import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

/**
 * Repository to access {@link InfaWorkflow} instances.
 * 
 * @author Girish lakshmanan
 */
@RepositoryRestResource(collectionResourceRel = "ptpworkflows",path = "ptpworkflows")
@Repository
public interface InfaPTPWorkflowRepository extends PagingAndSortingRepository<PTPWorkflow, Long> {
	
	/**
	 * Returns all {@link InfaWorkflow}s with the given {@link workflowName}.
	 * 
	 * @param workflowName must not be {@literal null}.
	 * @return
	 */
	
	Optional<PTPWorkflow> findByWorkflowName(@Param("workflowName") String workflowName);


}
