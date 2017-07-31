package com.globi.infa.datasource.gen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

@Configuration
@ConfigurationProperties("app.datasource.gen")
public class OracleGENConfig  extends HikariConfig {

	@Bean(name = "dsGEN")
	public DataSource dataSourceGEN() {
		return new HikariDataSource(this);
	}

	@Bean(name = "jdbcOracleGEN")
	@Autowired
	public JdbcTemplate jdbcTemplate(@Qualifier("dsGEN") DataSource dsGEN) {
		return new JdbcTemplate(dsGEN);
	}

}