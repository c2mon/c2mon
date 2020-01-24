package cern.c2mon.server.configuration.junit;

import cern.c2mon.cache.actions.process.ProcessService;
import org.junit.rules.ExternalResource;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;

@Named
public class StartTestProcessRule extends ExternalResource {

  private final ProcessService processService;

  @Inject
  public StartTestProcessRule(ProcessService processService) {
    this.processService = processService;
  }

  @Override
  protected void before() throws Throwable {
    super.before();
    processService.start(50L, "hostname", new Timestamp(System.currentTimeMillis()));
  }
}
