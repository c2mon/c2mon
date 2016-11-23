package cern.c2mon.daq.common;

import cern.c2mon.daq.common.conf.core.ConfigurationController;
import cern.c2mon.daq.common.messaging.JmsSender;
import cern.c2mon.daq.common.messaging.ProcessMessageReceiver;
import cern.c2mon.daq.common.messaging.ProcessRequestSender;
import cern.c2mon.daq.common.messaging.impl.DummyRequestSender;
import cern.c2mon.daq.common.messaging.impl.ProcessMessageSender;
import cern.c2mon.daq.filter.IFilterMessageSender;
import org.easymock.EasyMock;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

import static java.util.Collections.singletonList;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
public class ProcessMessageSenderMock {

  public ProcessMessageSender processMessageSender() {
    return EasyMock.createMock(ProcessMessageSender.class);
  }

  @Bean
  public ProcessRequestSender primaryRequestSender() {
    return new DummyRequestSender();
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

//  @Bean
//  public ConfigurationController configurationController() {
//    return EasyMock.createMock(ConfigurationController.class);
//  }
}
