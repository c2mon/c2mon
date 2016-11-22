package cern.c2mon.server.configuration.config;

import cern.c2mon.server.daqcommunication.out.ProcessCommunicationManager;
import org.easymock.EasyMock;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class ProcessCommunicationManagerMock {

  @Bean
  public ProcessCommunicationManager processCommunicationManager() {
    return EasyMock.createMock(ProcessCommunicationManager.class);
  }
}
