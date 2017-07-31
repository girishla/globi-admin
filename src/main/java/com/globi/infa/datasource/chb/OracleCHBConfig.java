package com.globi.infa.datasource.chb;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
@Configuration
@ConfigurationProperties("app.datasource.chb")
public class OracleCHBConfig  extends HikariConfig  {


	 @Bean(name = "dsCHB") 
	    public DataSource dataSourceCHB() { 
			return new HikariDataSource(this);
	    } 

    
    @Bean(name = "jdbcOracleCHB") 
    @Autowired
    public JdbcTemplate jdbcTemplate(@Qualifier("dsCHB") DataSource dsCHB) { 
        return new JdbcTemplate(dsCHB); 
    } 
    
}