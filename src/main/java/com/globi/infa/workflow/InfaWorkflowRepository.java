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
public interface InfaWorkflowRepository extends CrudRepository<InfaWorkflow, Long>, InfaWorkflowCreator<DataSourceTable> {
	
	/**
	 * Returns all {@link InfaWorkflow}s with the given {@link workflowName}.
	 * 
	 * @param workflowName must not be {@literal null}.
	 * @return
	 */
	Optional<InfaWorkflow> findByWorkflowName(@Param("workflowName") String workflowName);

	

}
