package cern.c2mon.client.core.config.mock;

import cern.c2mon.client.core.manager.SupervisionManager;
import org.easymock.EasyMock;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class CoreSupervisionManagerMock {

  @Bean
  public SupervisionManager supervisionManager() {
    return EasyMock.createMock(SupervisionManager.class);
  }
}
