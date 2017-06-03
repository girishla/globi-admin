package com.globi.infa.generator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.globi.infa.datasource.core.DataTypeMapper;
import com.globi.infa.datasource.core.SourceMetadataFactory;
import com.globi.infa.datasource.core.SourceMetadataFactoryMapper;
import com.globi.infa.datasource.core.TableColumnMetadataVisitor;
import com.globi.infa.datasource.core.TableColumnRepository;
import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.workflow.GeneratedWorkflow;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.metadata.sourcesystem.SourceSystemRepository;

import lombok.Setter;

public abstract class AbstractGenerationStrategy {

	@Autowired
	protected Jaxb2Marshaller marshaller;

	@Autowired
	protected SourceSystemRepository sourceSystemRepo;

	@Autowired
	protected SourceMetadataFactoryMapper metadataFactoryMapper;

	protected SourceMetadataFactory sourceMetadataFactory;

	protected PTPWorkflow wfDefinition;

	@Setter
	protected DataTypeMapper dataTypeMapper;
	@Setter
	protected TableColumnRepository colRepository;
	@Setter
	protected TableColumnMetadataVisitor columnQueryVisitor;

	protected final List<WorkflowCreatedEventListener> createdEventListeners = new ArrayList<>();;

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

	public void setWfDefinition(PTPWorkflow wfDefinition) {
		this.wfDefinition = wfDefinition;

		// get the correct factory based on the Source System Name.
		// Each Source needs a different Factory due to the inherent differences between them
		this.sourceMetadataFactory = this.metadataFactoryMapper.getMetadataFactoryMap()
				.get(wfDefinition.getSourceName());
		this.dataTypeMapper = sourceMetadataFactory.createDatatypeMapper();
		this.colRepository = sourceMetadataFactory.createTableColumnRepository();
		this.columnQueryVisitor = sourceMetadataFactory.createTableColumnMetadataVisitor();

	}

}
