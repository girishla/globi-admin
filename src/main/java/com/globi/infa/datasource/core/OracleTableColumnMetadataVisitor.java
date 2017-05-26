package com.globi.infa.datasource.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.globi.infa.datasource.core.InfaSourceColumnDefinition;
import com.globi.metadata.sourcesystem.SourceSystemRepository;

@Component
public class OracleTableColumnMetadataVisitor implements TableColumnMetadataVisitor {



	@Autowired
	protected OracleSourceDataTypeMapper mapper;

	protected String columnSQL = "SELECT \r\n" + 
			"COLUMN_ID COLUMN_NUMBER\r\n" + 
			",COLUMN_NAME\r\n" + 
			",DATA_TYPE\r\n" + 
			",CHAR_LENGTH PHYSICAL_LENGTH\r\n" + 
			",CASE WHEN DATA_TYPE ='DATE' THEN 19 WHEN DATA_TYPE='NUMBER' THEN DATA_PRECISION +2 WHEN DATA_TYPE='LONG' THEN 4000 ELSE 0 END COL_LENGTH\r\n" + 
			",CASE WHEN NULLABLE='Y' THEN 'NULL' ELSE 'NOTNULL' END NULLABLE\r\n" + 
			",CASE WHEN DATA_TYPE='DATE' THEN 19 WHEN DATA_TYPE IN ('VARCHAR2','CHAR') THEN CHAR_LENGTH WHEN DATA_TYPE='LONG' THEN 4000 ELSE DATA_PRECISION END COL_PRECISION\r\n" + 
			",NVL(DATA_SCALE,0) COL_SCALE\r\n" + 
			",CASE WHEN DATA_TYPE='DATE' THEN 19 ELSE 0 END COL_OFFSET\r\n" + 
			",sum(CHAR_LENGTH) over (ORDER BY COLUMN_ID\r\n" + 
			"ROWS BETWEEN UNBOUNDED PRECEDING\r\n" + 
			"AND CURRENT ROW)-(CHAR_LENGTH) PHYSICAL_OFFSET\r\n" + 
			"FROM ALL_TAB_COLUMNS WHERE TABLE_NAME=?";

	
	@Autowired
	SourceSystemRepository sourceSystemRepo;

	@Override
	public List<InfaSourceColumnDefinition> getAllColumnsFor(JdbcTemplate jdbcT, String tableName) {
		return jdbcT.query(columnSQL, tableColumnMapper, tableName);

	}

	private static Integer tryParse(String text) {
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	protected final RowMapper<InfaSourceColumnDefinition> tableColumnMapper = new RowMapper<InfaSourceColumnDefinition>() {
		public InfaSourceColumnDefinition mapRow(ResultSet rs, int rowNum) throws SQLException {

			InfaSourceColumnDefinition colDefn = InfaSourceColumnDefinition.builder()//
					.columnName(rs.getString("COLUMN_NAME"))//
					.columnLength(tryParse(rs.getString("COL_LENGTH")))//
					.columnNumber(tryParse(rs.getString("COLUMN_NUMBER")))//
					.columnDataType(mapper.mapType(rs.getString("DATA_TYPE")))//
					.nullable(rs.getString("NULLABLE"))//
					.offset(tryParse(rs.getString("COL_OFFSET")))//
					.physicalLength(tryParse(rs.getString("PHYSICAL_LENGTH")))//
					.physicalOffset(tryParse(rs.getString("PHYSICAL_OFFSET")))//
					.precision(tryParse(rs.getString("COL_PRECISION")))//
					.scale(tryParse(rs.getString("COL_SCALE")))//
					.integrationIdFlag(false).build();

			return colDefn;
		}
	};

	
	
	
	
	
	
	
	
}
