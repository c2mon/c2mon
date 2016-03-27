package cern.c2mon.web.configviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;

/**
 * @author Justin Lewis Salmon
 */
@SpringBootApplication
//@MapperScan("cern.c2mon.web.configviewer.statistics.daqlog")
public class Application extends SpringBootServletInitializer {

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Application.class, args);
  }
}
