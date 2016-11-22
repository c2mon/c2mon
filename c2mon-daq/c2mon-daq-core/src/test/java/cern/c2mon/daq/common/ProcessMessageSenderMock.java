package cern.c2mon.daq.common;

import cern.c2mon.daq.common.messaging.JmsSender;
import cern.c2mon.daq.common.messaging.ProcessMessageReceiver;
import cern.c2mon.daq.common.messaging.ProcessRequestSender;
import cern.c2mon.daq.common.messaging.impl.ProcessMessageSender;
import cern.c2mon.daq.filter.IFilterMessageSender;
import org.easymock.EasyMock;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;

import static java.util.Collections.singletonList;

/**
 * @author Justin Lewis Salmon
 */
public class ProcessMessageSenderMock {

  @Bean
  public ProcessRequestSender processRequestSender() {
    return EasyMock.createMock(ProcessRequestSender.class);
  }

  @Bean
  public ProcessMessageReceiver processMessageReceiver() {
    return EasyMock.createMock(ProcessMessageReceiver.class);
  }

  @Bean
  public IFilterMessageSender filterMessageSender() {
    return EasyMock.createMock(IFilterMessageSender.class);
  }

  @Bean
  public JmsSender jmsSender() {
    return EasyMock.createMock(JmsSender.class);
  }

  @Bean
  public InitializingBean setupMocks(ProcessMessageSender processMessageSender) {
    return () -> processMessageSender.setJmsSenders(singletonList(jmsSender()));
  }
}
