package com.example.sportsphere.db;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BazaPodataka {
  @Value("${spring.datasource.url}") private String url;

  @Value("${spring.datasource.username}") private String korisnickoIme;

  @Value("${spring.datasource.password}") private String lozinka;

  @Bean
  public DataSource ciljnaBaza() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
    dataSource.setUrl(url);
    dataSource.setUsername(korisnickoIme);
    dataSource.setPassword(lozinka);
    return dataSource;
  }

  @Bean
  @Primary
  public DataSource izvorPodataka(@Qualifier("ciljnaBaza") DataSource ciljnaBaza) {
    return new TransactionAwareDataSourceProxy(ciljnaBaza);
  }

  @Bean
  public PlatformTransactionManager transactionManager(
      @Qualifier("ciljnaBaza") DataSource ciljnaBaza) {
    return new DataSourceTransactionManager(ciljnaBaza);
  }
}
