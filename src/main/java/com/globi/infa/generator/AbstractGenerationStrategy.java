package com.globi.infa.generator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.globi.infa.datasource.core.SourceMetadataFactoryMapper;
import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.workflow.GeneratedWorkflow;
import com.globi.infa.workflow.InfaWorkflow;

public abstract class AbstractGenerationStrategy {

	protected final Jaxb2Marshaller marshaller;
	protected final SourceMetadataFactoryMapper metadataFactoryMapper;
	protected GeneratorContext generatorContext;

	
	protected final List<WorkflowCreatedEventListener> createdEventListeners = new ArrayList<>();;
	
	
	protected AbstractGenerationStrategy(Jaxb2Marshaller marshaller,SourceMetadataFactoryMapper metadataFactoryMapper){
	
		this.marshaller=marshaller;
		this.metadataFactoryMapper=metadataFactoryMapper;
		
	}
	
	

	public void addListener(WorkflowCreatedEventListener listener) {
		if (!createdEventListeners.contains(listener)) {
			createdEventListeners.add(listener);
		}
	}

	public void removeListener(WorkflowCreatedEventListener listener) {
		createdEventListeners.remove(listener);
	}

	protected void notifyListeners(InfaPowermartObject pmo, GeneratedWorkflow wf) {
		for (WorkflowCreatedEventListener listener : createdEventListeners) {
			listener.notify(pmo, wf);
		}
	}
	
	
	
	public void setContext(String sourceName,InfaWorkflow inputWF) {
		
		generatorContext=GeneratorContext.contextFor(sourceName, metadataFactoryMapper,inputWF);

	}
	



}
