package com.globi.infa.datasource.chb;

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

import com.globi.infa.datasource.type.oracle.OracleTableColumnMetadataVisitor;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;

@RestController
public class CHBTableColumnController {

	@Autowired	
	private final CHBTableColumnRepository repository;
	
	@Autowired
	private OracleTableColumnMetadataVisitor columnQueryVisitor;

	@Autowired
	@Qualifier("dsCHB")
	private DataSource dataSource;

	@Autowired
	public CHBTableColumnController(CHBTableColumnRepository repo) {
		repository = repo;
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/datasources/chb/tables/{tableName}/columns")
	public @ResponseBody ResponseEntity<?> getTableColumns(@PathVariable String tableName) {

		List<InfaSourceColumnDefinition> columns = repository.accept(columnQueryVisitor,tableName.toUpperCase());
		return ResponseEntity.ok(columns);
	}

}