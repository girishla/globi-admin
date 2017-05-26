package com.globi.infa.datasource.fbm;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.globi.infa.datasource.core.OracleTableColumnMetadataVisitor;
import com.globi.infa.datasource.core.InfaSourceColumnDefinition;

@RestController
public class FBMTableColumnController {

	@Autowired	
	private final FBMTableColumnRepository repository;
	
	@Autowired
	private OracleTableColumnMetadataVisitor columnQueryVisitor;

	@Autowired
	@Qualifier("dsFBM")
	private DataSource dataSource;

	@Autowired
	public FBMTableColumnController(FBMTableColumnRepository repo) {
		repository = repo;
	}

	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/datasources/fbm/tables/{tableName}/columns")
	public @ResponseBody ResponseEntity<?> getTableColumns(@PathVariable String tableName) {

		List<InfaSourceColumnDefinition> columns = repository.accept(columnQueryVisitor,tableName.toUpperCase());
		return ResponseEntity.ok(columns);
	}

}