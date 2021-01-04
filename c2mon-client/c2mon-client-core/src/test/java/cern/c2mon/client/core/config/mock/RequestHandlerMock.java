package cern.c2mon.client.core.config.mock;

import cern.c2mon.client.core.jms.RequestHandler;
import org.easymock.EasyMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * @author Justin Lewis Salmon
 */
public class RequestHandlerMock {

  @Bean @Primary
  public RequestHandler coreRequestHandler() {
    return EasyMock.createMock(RequestHandler.class);
  }
}
