package com.globi.infa.datasource.gen;

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
import com.globi.infa.datasource.gen.GENTableColumnRepository;
import com.globi.infa.datasource.core.InfaSourceColumnDefinition;

@RestController
public class GENTableColumnController {

	@Autowired	
	private final GENTableColumnRepository repository;
	
	@Autowired
	private OracleTableColumnMetadataVisitor columnQueryVisitor;

	@Autowired
	@Qualifier("dsGEN")
	private DataSource dataSource;

	@Autowired
	public GENTableColumnController(GENTableColumnRepository repo) {
		repository = repo;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/datasources/gen/tables/{tableName}/columns")
	public @ResponseBody ResponseEntity<?> getTableColumns(@PathVariable String tableName) {

		List<InfaSourceColumnDefinition> columns = repository.accept(columnQueryVisitor,tableName.toUpperCase());
		return ResponseEntity.ok(columns);
	}

}