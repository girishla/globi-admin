package com.globi.infa.datasource.vpt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class SQLServerVPTConfig {
	
	
	
	

	@Bean(name = "dsVPT")
	@ConfigurationProperties("app.datasource.vpt")
	public DataSource dataSourceVPT() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "jdbcSQLServerVPT")
	@Autowired
	public JdbcTemplate jdbcTemplate(@Qualifier("dsVPT") DataSource dsVPT) {
		return new JdbcTemplate(dsVPT);
	}

}