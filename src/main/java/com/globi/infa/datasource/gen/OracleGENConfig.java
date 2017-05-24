package com.globi.infa.datasource.gen;

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
public class OracleGENConfig {
	
	
	
	

	@Bean(name = "dsGEN")
	@ConfigurationProperties("app.datasource.gen")
	public DataSource dataSourceGEN() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "jdbcOracleGEN")
	@Autowired
	public JdbcTemplate jdbcTemplate(@Qualifier("dsGEN") DataSource dsGEN) {
		return new JdbcTemplate(dsGEN);
	}

}