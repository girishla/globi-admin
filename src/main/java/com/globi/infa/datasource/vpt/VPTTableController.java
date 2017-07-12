package com.globi.infa.datasource.vpt;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.globi.infa.datasource.core.DataSourceTableDTO;
import com.globi.infa.datasource.type.sqlserver.SQLServerTableMetadataVisitor;

@RestController
public class VPTTableController {

	
	@Autowired
	private SQLServerTableMetadataVisitor tblQueryVisitor;
	
	@Autowired
    private final VPTTableRepository repository;

    @Autowired
    public VPTTableController(VPTTableRepository repo) { 
        repository = repo;
    }

    @RequestMapping(method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/datasources/vpt/tables") 
    public @ResponseBody ResponseEntity<?> getTables() {
    	
        List<DataSourceTableDTO> tables = repository.accept(tblQueryVisitor);
        return ResponseEntity.ok(tables); 
    }

}