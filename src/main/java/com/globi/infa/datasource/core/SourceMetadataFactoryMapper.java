package com.globi.infa.datasource.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
public class SourceMetadataFactoryMapper {

	@Autowired
	private List<SourceMetadataFactory> sourceMetadataFactoryList;

	@Getter
	private final Map<String, SourceMetadataFactory> metadataFactoryMap = new HashMap<>();;

	public SourceMetadataFactoryMapper(List<SourceMetadataFactory> sourceMetadataFactoryList) {

		this.sourceMetadataFactoryList = sourceMetadataFactoryList;
		this.sourceMetadataFactoryList.forEach(sourceMetadataFactory -> {

			metadataFactoryMap.put(sourceMetadataFactory.getSourceName(), sourceMetadataFactory);
		});

	}

}
