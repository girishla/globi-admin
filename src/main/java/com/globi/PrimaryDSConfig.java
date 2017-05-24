package com.globi;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class PrimaryDSConfig {

	
	@Bean(name = "dsMDT")
	@ConfigurationProperties("app.datasource.mdt")
	@Primary
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	
}
