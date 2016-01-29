//package cern.c2mon.web.manager.config;
//
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.mybatis.spring.SqlSessionFactoryBean;
//import org.mybatis.spring.annotation.MapperScan;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
//import org.springframework.context.annotation.*;
//import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
//import org.springframework.core.env.Environment;
//import org.springframework.core.io.ClassPathResource;
//
//import javax.sql.DataSource;
//
///**
// * @author Justin Lewis Salmon
// */
//@Configuration
//@PropertySources({
//    @PropertySource("${c2mon.client.conf.url}"),
//    @PropertySource("${c2mon.web.conf.url}")
//})
//public class MybatisConfig {
//
//  @Autowired
//  private Environment environment;
//
//  @Bean
//  public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
//    return new PropertySourcesPlaceholderConfigurer();
//  }
//
//  @Bean
//  @Primary
//  public DataSource stlogDataSource() {
//    return DataSourceBuilder.create()
//        .url(environment.getRequiredProperty("c2mon.jdbc.url"))
//        .username(environment.getRequiredProperty("c2mon.jdbc.stlog.user"))
//        .password(environment.getRequiredProperty("c2mon.jdbc.stlog.password")).build();
//  }
//
//  @Bean
//  public DataSource daqlogDataSource() {
//    return DataSourceBuilder.create()
//        .url(environment.getRequiredProperty("c2mon.jdbc.url"))
//        .username(environment.getRequiredProperty("c2mon.jdbc.daqlog.user"))
//        .password(environment.getRequiredProperty("c2mon.jdbc.daqlog.password")).build();
//  }
//
//  @Bean
//  public SqlSessionFactory stlogSqlSessionFactory() throws Exception {
//    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
//    sessionFactory.setDataSource(stlogDataSource());
//    sessionFactory.setConfigLocation(new ClassPathResource("mybatis/mybatis-config.xml"));
//    return sessionFactory.getObject();
//  }
//
//  @Bean
//  public SqlSessionFactory daqlogSqlSessionFactory() throws Exception {
//    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
//    sessionFactory.setDataSource(stlogDataSource());
//    sessionFactory.setConfigLocation(new ClassPathResource("mybatis/mybatis-config.xml"));
//    return sessionFactory.getObject();
//  }
//}
