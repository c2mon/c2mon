package cern.c2mon.web.configviewer;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * @author Justin Lewis Salmon
 */
@SpringBootApplication
//@MapperScan("cern.c2mon.web.configviewer.statistics.daqlog")
public class Application extends SpringBootServletInitializer {

  public static void main(String[] args) throws Exception {
    new SpringApplicationBuilder().bannerMode(Banner.Mode.OFF).sources(Application.class).run(args);
  }
}
