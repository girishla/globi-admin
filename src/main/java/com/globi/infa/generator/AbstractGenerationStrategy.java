package com.globi.infa.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.globi.infa.datasource.core.DataTypeMapper;
import com.globi.infa.datasource.core.SourceMetadataFactory;
import com.globi.infa.datasource.core.SourceMetadataFactoryMapper;
import com.globi.infa.datasource.core.TableColumnMetadataVisitor;
import com.globi.infa.datasource.core.TableColumnRepository;
import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.notification.messages.WorkflowMessageNotifier;
import com.globi.infa.notification.messages.WorkflowNotificationContentMessage;
import com.globi.infa.workflow.GeneratedWorkflow;
import com.globi.metadata.sourcesystem.SourceSystemRepository;

import lombok.Setter;

public abstract class AbstractGenerationStrategy {

	protected final Jaxb2Marshaller marshaller;
	protected final SourceSystemRepository sourceSystemRepo;
	protected final SourceMetadataFactoryMapper metadataFactoryMapper;
	protected WorkflowMessageNotifier socketNotifier;
	
	protected SourceMetadataFactory sourceMetadataFactory;


	@Setter
	protected DataTypeMapper dataTypeMapper;
	
	@Setter
	protected DataTypeMapper sourceToTargetDataTypeMapper;
	
	
	@Setter
	protected TableColumnRepository colRepository;
	@Setter
	protected TableColumnMetadataVisitor columnQueryVisitor;

	protected final List<WorkflowCreatedEventListener> createdEventListeners = new ArrayList<>();;
	
	
	AbstractGenerationStrategy(Jaxb2Marshaller marshaller, SourceSystemRepository sourceSystemRepo,SourceMetadataFactoryMapper metadataFactoryMapper,WorkflowMessageNotifier socketNotifier){
	
		this.marshaller=marshaller;
		this.sourceSystemRepo=sourceSystemRepo;
		this.metadataFactoryMapper=metadataFactoryMapper;
		this.socketNotifier=socketNotifier;
		
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
	

	
	
	
	public void setContext(String sourceName) {

		// get the correct factory based on the Source System Name.
		// Each Source needs a different Factory due to the inherent differences between them
		this.sourceMetadataFactory = this.metadataFactoryMapper.getMetadataFactoryMap()
				.get(sourceName);
		this.dataTypeMapper = sourceMetadataFactory.createDatatypeMapper();
		this.sourceToTargetDataTypeMapper=sourceMetadataFactory.createSourceToTargetDatatypeMapper();
		this.colRepository = sourceMetadataFactory.createTableColumnRepository();
		this.columnQueryVisitor = sourceMetadataFactory.createTableColumnMetadataVisitor();

	}
	



}
