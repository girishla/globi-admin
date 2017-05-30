package com.globi.infa.datasource.gen;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.globi.infa.datasource.core.DataSourceTableDTO;
import com.globi.infa.datasource.core.OracleTableMetadataVisitor;

@RestController
public class GENTableController {

	
	@Autowired
	private OracleTableMetadataVisitor tblQueryVisitor;
	
	@Autowired
    private final GENTableRepository repository;

    @Autowired
    public GENTableController(GENTableRepository repo) { 
        repository = repo;
    }

    @RequestMapping(method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/datasources/gen/tables") 
    public @ResponseBody ResponseEntity<?> getTables() {
    	
        List<DataSourceTableDTO> tables = repository.accept(tblQueryVisitor);
        return ResponseEntity.ok(tables); 
    }

}