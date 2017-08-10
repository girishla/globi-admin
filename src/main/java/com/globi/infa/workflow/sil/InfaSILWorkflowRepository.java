package com.globi.infa.workflow.sil;


import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import com.globi.infa.workflow.InfaWorkflow;

/**
 * Repository to access {@link InfaWorkflow} instances.
 * 
 * @author Girish lakshmanan
 */
@RepositoryRestResource(collectionResourceRel = "silworkflows",path = "silworkflows")
@Repository
public interface InfaSILWorkflowRepository extends PagingAndSortingRepository<SILWorkflow, Long> {
	
	/**
	 * Returns all {@link InfaWorkflow}s with the given {@link workflowName}.
	 * 
	 * @param workflowName must not be {@literal null}.
	 * @return
	 */
	
	Optional<SILWorkflow> findByWorkflowName(@Param("workflowName") String workflowName);


}
