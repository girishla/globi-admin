package com.globi.infa.datasource.type.oracle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.globi.infa.datasource.core.ObjectNameNormaliser;
import com.globi.infa.datasource.core.TableColumnMetadataVisitor;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.metadata.sourcesystem.SourceSystemRepository;

@Component
public class OracleTableColumnMetadataVisitor implements TableColumnMetadataVisitor {



	@Autowired
	protected OracleToInfaSourceDataTypeMapper mapper;

	protected String columnSQL = "SELECT COLUMN_ID COLUMN_NUMBER\r\n" + 
			"			,COLUMN_NAME\r\n" + 
			"			,DATA_TYPE\r\n" + 
			"			,CASE WHEN DATA_TYPE IN ('NUMBER','CLOB') THEN DATA_LENGTH WHEN DATA_TYPE IN ('DATE','TIMESTAMP(6)') THEN 19  ELSE CHAR_LENGTH END PHYSICAL_LENGTH\r\n" + 
			"			,CASE WHEN DATA_TYPE IN ('DATE','TIMESTAMP(6)') THEN 19 WHEN DATA_TYPE IN ('NUMBER','CLOB') THEN DATA_LENGTH WHEN DATA_TYPE='LONG' THEN 4000 ELSE 0 END COL_LENGTH\r\n" + 
			"			,CASE WHEN NULLABLE='Y' THEN 'NULL' ELSE 'NOTNULL' END NULLABLE\r\n" + 
			"			,CASE WHEN DATA_TYPE IN ('DATE','TIMESTAMP(6)') THEN 19 WHEN DATA_TYPE IN ('VARCHAR2','CHAR') THEN CHAR_LENGTH WHEN DATA_TYPE='LONG' THEN 4000 WHEN DATA_TYPE IN ('NUMBER','CLOB') THEN DATA_LENGTH ELSE DATA_PRECISION END COL_PRECISION\r\n" + 
			"			,CASE WHEN DATA_TYPE IN ('DATE','TIMESTAMP(6)') THEN 0 ELSE NVL(DATA_SCALE,0) END COL_SCALE\r\n" + 
			"			,CASE WHEN DATA_TYPE IN ('DATE','TIMESTAMP(6)') THEN 19 ELSE 0 END COL_OFFSET\r\n" + 
			"			,sum(CHAR_LENGTH) over (ORDER BY COLUMN_ID\r\n" + 
			"			ROWS BETWEEN UNBOUNDED PRECEDING\r\n" + 
			"			AND CURRENT ROW)-(CHAR_LENGTH) PHYSICAL_OFFSET\r\n" + 
			"			FROM ALL_TAB_COLUMNS WHERE TABLE_NAME=?";
	
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

			
			InfaSourceColumnDefinition colDefn = InfaSourceColumnDefinition//
					.builder()//
					.columnName(ObjectNameNormaliser.normalise(rs.getString("COLUMN_NAME")))//
					.columnLength(tryParse(rs.getString("COL_LENGTH")))//
					.columnNumber(tryParse(rs.getString("COLUMN_NUMBER")))//
					.columnDataType(mapper.mapType(rs.getString("DATA_TYPE").toUpperCase()))//
					.nullable(rs.getString("NULLABLE"))//
					.offset(tryParse(rs.getString("COL_OFFSET")))//
					.physicalLength(tryParse(rs.getString("PHYSICAL_LENGTH")))//
					.physicalOffset(tryParse(rs.getString("PHYSICAL_OFFSET")))//
					.precision(tryParse(rs.getString("COL_PRECISION")))//
					.scale(tryParse(rs.getString("COL_SCALE")))//
					.build();

			return colDefn;
		}
	};

	
	
	
	
	
	
	
	
}
