package cern.c2mon.client.core.config.mock;

import cern.c2mon.client.core.manager.SupervisionServiceImpl;
import org.easymock.EasyMock;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class CoreSupervisionManagerMock {

  @Bean
  public SupervisionServiceImpl supervisionManager() {
    return EasyMock.createMock(SupervisionServiceImpl.class);
  }
}
