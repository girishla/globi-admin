package com.globi;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = "coreEntityManagerFactory",
transactionManagerRef = "coreTransactionManager")
public class JPAConfig {

	@Autowired
	@Qualifier("dsMDT")
	private DataSource dsMDT;

	@Bean
	PlatformTransactionManager coreTransactionManager() {
		return new JpaTransactionManager(coreEntityManagerFactory().getObject());
	}

	@Bean
	LocalContainerEntityManagerFactoryBean coreEntityManagerFactory() {

		HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
		
		jpaVendorAdapter.setShowSql(true);

		
		LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();

		factoryBean.setDataSource(dsMDT);
		factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
		factoryBean.setPackagesToScan(JPAConfig.class.getPackage().getName());
		factoryBean.setJpaProperties(additionalProperties());
		
		
		return factoryBean;
	}
	
	
	   Properties additionalProperties() {
		      Properties properties = new Properties();
		      properties.setProperty("hibernate.hbm2ddl.auto", "none");
		      properties.setProperty("hibernate.dialect", "org.hibernate.dialect.Oracle12cDialect");
		      return properties;
		   }

}
