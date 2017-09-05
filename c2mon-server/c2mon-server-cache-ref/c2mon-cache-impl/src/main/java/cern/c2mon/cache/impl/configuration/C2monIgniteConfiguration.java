package cern.c2mon.cache.impl.configuration;

import org.apache.ignite.IgniteSpringBean;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Szymon Halastra
 */
@Configuration
@ComponentScan("cern.c2mon.cache.impl")
public class C2monIgniteConfiguration {

  private IgniteConfiguration configureIgnite() {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("ignite-config.xml");

    IgniteConfiguration config = (IgniteConfiguration) context.getBean("base-ignite.cfg");

    config.setClientMode(true);

    return config;
  }

  @Bean(name = "C2monIgnite")
  public IgniteSpringBean createIgniteSpringBean() {
    IgniteSpringBean igniteSpringBean = new IgniteSpringBean();
    igniteSpringBean.setConfiguration(configureIgnite());
    return igniteSpringBean;
  }
}
