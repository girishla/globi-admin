package com.globi.infa.generator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.globi.infa.datasource.core.MetadataFactoryMapper;
import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.workflow.GeneratedWorkflow;
import com.globi.infa.workflow.InfaWorkflow;

public abstract class AbstractGenerationStrategy {

	protected final Jaxb2Marshaller marshaller;
	protected final MetadataFactoryMapper metadataFactoryMapper;
	protected PTPGeneratorContext generatorContext;

	
	protected final List<WorkflowCreatedEventListener> createdEventListeners = new ArrayList<>();;
	
	
	protected AbstractGenerationStrategy(Jaxb2Marshaller marshaller,MetadataFactoryMapper metadataFactoryMapper){
	
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
	
	
	
	public void setContext(String sourceName,String tblName,InfaWorkflow inputWF) {
		
		generatorContext=PTPGeneratorContext.contextFor(sourceName,tblName, metadataFactoryMapper,inputWF);

	}
	



}
