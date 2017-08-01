package com.globi.infa.metadata.core;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.globi.infa.metadata.srcprefix.SourceTablePrefix;
import com.globi.infa.metadata.srcprefix.SourceTablePrefixRepository;

@Component
public class SourceTableAbbreviationMap implements StringMap {


	private SourceTablePrefixRepository prefixRepo;

	public SourceTableAbbreviationMap(SourceTablePrefixRepository prefixRepo) {
		
		this.prefixRepo=prefixRepo;

	}

	@Override
	public String map(String tableUniqueName) {

		Optional<SourceTablePrefix> prefixRepositoryResult;
		String prefix = "";

		prefixRepositoryResult = prefixRepo.findByTableUniqueName(tableUniqueName);

		if (prefixRepositoryResult.isPresent()) {
			prefix = prefixRepositoryResult.get().getTablePrefix() + ":";
			
		};

		return prefix;

	}

}
