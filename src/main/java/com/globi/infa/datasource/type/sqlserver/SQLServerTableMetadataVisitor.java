package com.globi.infa.datasource.type.sqlserver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.globi.infa.datasource.core.DataSourceTableDTO;
import com.globi.infa.datasource.core.TableMetadataVisitor;


@Component
public class SQLServerTableMetadataVisitor implements TableMetadataVisitor {

	protected String tableSQL="select TABLE_SCHEMA AS OWNER, TABLE_NAME from INFORMATION_SCHEMA.TABLES ORDER BY TABLE_NAME";
	
	
	protected static final RowMapper<DataSourceTableDTO> tableMapper = new RowMapper<DataSourceTableDTO>() {
		public DataSourceTableDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
			DataSourceTableDTO table = new DataSourceTableDTO(rowNum, rs.getString("OWNER"), rs.getString("TABLE_NAME"),"");
			return table;
		}
	};
	
	@Override
	public List<DataSourceTableDTO> getAllTables(JdbcTemplate jdbcT,String ownerName) {

			return  jdbcT.query(tableSQL, tableMapper, ownerName);
	}

}
