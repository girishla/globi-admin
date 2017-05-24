package com.globi.infa.datasource.lnicrm;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
@Configuration
public class OracleLNICRMConfig {


	 @Bean(name = "dsLNICRM") 
	 @ConfigurationProperties("app.datasource.lnicrm")
	    public DataSource dataSourceLNICRM() { 
		  return DataSourceBuilder
			        .create()
			        .build();
	    } 

    
    @Bean(name = "jdbcOracleLNICRM") 
    @Autowired
    public JdbcTemplate jdbcTemplate(@Qualifier("dsLNICRM") DataSource dsLNICRM) { 
        return new JdbcTemplate(dsLNICRM); 
    } 
    
}