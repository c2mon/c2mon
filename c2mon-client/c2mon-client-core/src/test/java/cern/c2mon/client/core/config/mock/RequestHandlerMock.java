package cern.c2mon.client.core.config.mock;

import cern.c2mon.client.core.jms.RequestHandler;
import org.easymock.EasyMock;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class RequestHandlerMock {

  @Bean
  public RequestHandler coreRequestHandler() {
    return EasyMock.createMock(RequestHandler.class);
  }
}
