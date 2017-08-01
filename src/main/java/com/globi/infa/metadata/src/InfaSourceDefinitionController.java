package com.globi.infa.metadata.src;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.globi.infa.datasource.core.DataTypeMapper;
import com.globi.infa.datasource.core.MetadataFactoryMapper;
import com.globi.infa.datasource.core.SourceMetadataFactory;
import com.globi.infa.datasource.core.TableColumnMetadataVisitor;
import com.globi.infa.datasource.core.TableColumnRepository;
import com.globi.metadata.sourcesystem.SourceSystem;
import com.globi.metadata.sourcesystem.SourceSystemRepository;
import com.globi.rest.api.exceptions.BadRequestRestApiException;

@Controller
public class InfaSourceDefinitionController {

	@Autowired
	InfaSourceDefinitionRepository sourceDefnRepo;

	@Autowired
	MetadataFactoryMapper metadataFactoryMapper;
	
	@Autowired
	SourceSystemRepository sourceSystemRepo;

	private TableColumnRepository colRepository;

	private TableColumnMetadataVisitor columnQueryVisitor;

	private SourceMetadataFactory sourceMetadataFactory;

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/sourcetables/pull")
	public @ResponseBody ResponseEntity<?> pullSourceDefinition(
			@RequestBody @Valid InfaSourceDefinitionPullDTO sourcePullDTO) throws Exception {

		String tblName = sourcePullDTO.tableName;
		String sourceName = sourcePullDTO.sourceName;
		
		Optional<SourceSystem> source = sourceSystemRepo.findByName(sourceName);
		
		if (!(source.isPresent())) {

			throw new BadRequestRestApiException()
					.developerMessage("Source System not found in S_LAW_SRC")//
					.userMessage("Invalid source system. Please verify if the source system has been defined");
		}

		this.setSourceContext(sourcePullDTO.sourceName);

		InfaSourceDefinition sourceTableDef = InfaSourceDefinition.builder()//
				.sourceTableName(tblName)//
				.ownerName(source.get().getOwnerName())//
				.databaseName(source.get().getName())//
				.databaseType(source.get().getDbType())//
				.sourceTableUniqueName(source.get().getName() + "_" + tblName).build();
		List<InfaSourceColumnDefinition> allTableColumns = colRepository.accept(columnQueryVisitor,
				sourcePullDTO.tableName);

		if (allTableColumns.isEmpty()) {

			throw new BadRequestRestApiException()
					.developerMessage("Data repository returned no columns for the provided inputs")//
					.userMessage("The source table %s was not found in %s", sourcePullDTO.tableName,
							sourcePullDTO.sourceName);
		}

		Optional<InfaSourceDefinition> existingSourceTable = sourceDefnRepo
				.findBySourceTableUniqueName(sourcePullDTO.sourceName + "_" + sourcePullDTO.tableName);
		if (existingSourceTable.isPresent()) {
			existingSourceTable.get().getColumns().clear();
			InfaSourceDefinition cleanedSourceDefn = sourceDefnRepo.save(existingSourceTable.get());
			sourceTableDef.setId(cleanedSourceDefn.getId());
			sourceTableDef.setVersion(cleanedSourceDefn.getVersion());
		}
		sourceTableDef.getColumns().addAll(allTableColumns);
		sourceTableDef=sourceDefnRepo.save(sourceTableDef);


		return new ResponseEntity<InfaSourceDefinition>(sourceTableDef, HttpStatus.CREATED);
	}

	private void setSourceContext(String sourceName) {

		// get the correct factory based on the Source System Name.
		// Each Source needs a different Factory due to the inherent differences
		// between them
		this.sourceMetadataFactory = this.metadataFactoryMapper.getMetadataFactoryMap().get(sourceName);
		this.colRepository = sourceMetadataFactory.createTableColumnRepository();
		this.columnQueryVisitor = sourceMetadataFactory.createTableColumnMetadataVisitor();

	}

}
