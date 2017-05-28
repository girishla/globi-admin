package com.globi.infa.workflow;


import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Repository to access {@link InfaWorkflow} instances.
 * 
 * @author Girish lakshmanan
 */
@RepositoryRestResource
public interface PTPWorkflowRepository extends CrudRepository<PTPWorkflow, Long> {
	
	/**
	 * Returns all {@link InfaWorkflow}s with the given {@link workflowName}.
	 * 
	 * @param workflowName must not be {@literal null}.
	 * @return
	 */
//	Optional<PTPWorkflow> findByWorkflowName(@Param("workflowName") String workflowName);

	

}
