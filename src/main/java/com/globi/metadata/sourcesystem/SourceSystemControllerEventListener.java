package com.globi.metadata.sourcesystem;

import org.springframework.data.rest.core.event.AbstractRepositoryEventListener;
import org.springframework.stereotype.Component;

/*
* Event listener to reject {@code POST} requests to Spring Data REST.
* 
* @author Girish Lakshmanan
*/
@Component
class SourceSystemControllerEventListener extends AbstractRepositoryEventListener<SourceSystem> {


	@Override
	protected void onBeforeCreate(SourceSystem sourceSystem) {
		//throw new IllegalArgumentException("name cannot be empty");
	}
}
