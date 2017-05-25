package com.globi.infa.workflow;


import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.globi.infa.DataSourceTable;

/**
 * Repository to access {@link InfaWorkflow} instances.
 * 
 * @author Girish lakshmanan
 */
@RepositoryRestResource
public interface PTPWorkflowRepository extends CrudRepository<PTPWorkflow, Long>, InfaWorkflowCreator<PTPWorkflow> {
	
	/**
	 * Returns all {@link InfaWorkflow}s with the given {@link workflowName}.
	 * 
	 * @param workflowName must not be {@literal null}.
	 * @return
	 */
//	Optional<PTPWorkflow> findByWorkflowName(@Param("workflowName") String workflowName);

	

}
