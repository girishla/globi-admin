package com.globi.infa.datasource.type.sqlserver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.globi.infa.datasource.core.TableColumnMetadataVisitor;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;

@Component
public class SQLServerTableColumnMetadataVisitor implements TableColumnMetadataVisitor {



	protected String columnSQL = "SELECT table_name, \r\n" + 
			"       column_name                                    AS COL_NAME, \r\n" + 
			"       CASE \r\n" + 
			"         WHEN data_type IN ( 'int' ) THEN numeric_precision \r\n" + 
			"         WHEN data_type IN ( 'bit' ) THEN 1 \r\n" + 
			"         WHEN data_type IN ( 'datetime' ) THEN 19 \r\n" + 
			"         ELSE 0 \r\n" + 
			"       END                                            COL_LENGTH, \r\n" + 
			"       ordinal_position                               COL_NUMBER, \r\n" + 
			"       data_type, \r\n" + 
			"       CASE \r\n" + 
			"         WHEN is_nullable = 'NO' THEN 'NONNULL' \r\n" + 
			"         ELSE 'NULL' \r\n" + 
			"       END                                            NULLABLE, \r\n" + 
			"       SUM(Isnull(numeric_precision, 0)) \r\n" + 
			"         over( \r\n" + 
			"           PARTITION BY table_name \r\n" + 
			"           ORDER BY ordinal_position ROWS BETWEEN unbounded preceding AND \r\n" + 
			"         CURRENT ROW) \r\n" + 
			"                                                      AS COL_OFFSET, \r\n" + 
			"       CASE \r\n" + 
			"         WHEN data_type = 'timestamp' THEN 8 \r\n" + 
			"         WHEN data_type = 'bit' THEN 1 \r\n" + 
			"         WHEN data_type = 'datetime' THEN 23 \r\n" + 
			"         ELSE Coalesce(character_maximum_length, numeric_precision, \r\n" + 
			"              datetime_precision) \r\n" + 
			"       END                                            PHYSICAL_LENGTH, \r\n" + 
			"       SUM(CASE \r\n" + 
			"             WHEN data_type = 'timestamp' THEN 8 \r\n" + 
			"             WHEN data_type = 'bit' THEN 1 \r\n" + 
			"             WHEN data_type = 'datetime' THEN 23 \r\n" + 
			"             ELSE Coalesce(character_maximum_length, numeric_precision, \r\n" + 
			"                  datetime_precision) \r\n" + 
			"           END) \r\n" + 
			"         over( \r\n" + 
			"           PARTITION BY table_name \r\n" + 
			"           ORDER BY ordinal_position ROWS BETWEEN unbounded preceding AND \r\n" + 
			"         CURRENT ROW) \r\n" + 
			"                                                      AS PHYSICAL_OFFSET, \r\n" + 
			"       CASE \r\n" + 
			"         WHEN data_type = 'timestamp' THEN 8 \r\n" + 
			"         WHEN data_type = 'bit' THEN 1 \r\n" + 
			"         WHEN data_type = 'datetime' THEN 23 \r\n" + 
			"         ELSE Coalesce(character_maximum_length, numeric_precision, \r\n" + 
			"              datetime_precision) \r\n" + 
			"       END                                            COL_PRECISION, \r\n" + 
			"       Coalesce(numeric_scale, datetime_precision, 0) COL_SCALE \r\n" + 
			"FROM   information_schema.COLUMNS \r\n" + 
			"WHERE  table_name = ?";
	

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
					.columnName(rs.getString("COL_NAME"))//
					.columnLength(tryParse(rs.getString("COL_LENGTH")))//
					.columnNumber(tryParse(rs.getString("COL_NUMBER")))//
					.columnDataType(rs.getString("DATA_TYPE"))//
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
