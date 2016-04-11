/*******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package cern.c2mon.daq.config;

import cern.c2mon.daq.common.conf.core.ConfigurationController;
import cern.c2mon.daq.common.messaging.JmsLifecycle;
import cern.c2mon.daq.common.messaging.JmsSender;
import cern.c2mon.daq.common.messaging.impl.ActiveJmsSender;
import cern.c2mon.daq.common.messaging.impl.DummyJmsSender;
import cern.c2mon.daq.common.messaging.impl.ProcessMessageSender;
import cern.c2mon.daq.common.messaging.impl.ProxyJmsSender;
import cern.c2mon.daq.filter.FilterMessageSender;
import cern.c2mon.daq.filter.IFilterMessageSender;
import cern.c2mon.daq.filter.impl.ActiveFilterSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;

import java.util.Arrays;
import java.util.Collections;

/**
 * This configuration class is responsible for instantiating the various {@link ProcessMessageSender} beans used within the DAQ core. The {@link
 * ProcessMessageSender}s handle the actual sending of tags etc. to the JMS broker(s).
 *
 * There are three possible modes (specified by the c2mon.daq.mode, which is translated to a Spring {@link Profile}). The three modes are:
 *
 * "single" : a single {@link ProcessMessageSender} sending to a primary JMS broker
 * "double" : two {@link ProcessMessageSender}s, one sending to a primary JMS broker and one sending to a secondary broker (e.g. a test server)
 * "test"   : a single {@link ProcessMessageSender} which connects to a primary JMS broker but does not actually send anything.
 *
 * @author Justin Lewis Salmon
 */
@Configuration
@ImportResource({
    "classpath:daq-activemq.xml",
    "classpath:daq-activemq-second.xml"
})
public class ProcessMessageSenderConfig {

  @Autowired
  ConfigurationController configurationController;

  @Autowired
  @Qualifier("sourceUpdateJmsTemplate")
  JmsTemplate sourceUpdateJmsTemplate;

  @Autowired
  @Qualifier("secondSourceUpdateJmsTemplate")
  JmsTemplate secondSourceUpdateJmsTemplate;

  @Bean
  @Profile("single")
  public ProcessMessageSender singleMessageSender() {
    ProcessMessageSender processMessageSender = new ProcessMessageSender(configurationController);
    processMessageSender.setJmsSenders(Collections.singletonList(activeJmsSender()));
    return processMessageSender;
  }

  @Bean
  @Profile("double")
  public ProcessMessageSender doubleMessageSender() {
    ProcessMessageSender processMessageSender = new ProcessMessageSender(configurationController);
    processMessageSender.setJmsSenders(Arrays.asList(activeJmsSender(), proxyJmsSender()));
    return processMessageSender;
  }

  @Bean
  @Profile("test")
  public ProcessMessageSender testMessageSender() {
    ProcessMessageSender processMessageSender = new ProcessMessageSender(configurationController);
    processMessageSender.setJmsSenders(Collections.singletonList(dummyJmsSender()));
    return processMessageSender;
  }

  @Bean
  public JmsSender activeJmsSender() {
    return new ActiveJmsSender(configurationController, sourceUpdateJmsTemplate);
  }

  @Bean
  public JmsSender secondActiveJmsSender() {
    return new ActiveJmsSender(configurationController, secondSourceUpdateJmsTemplate);
  }

  @Bean
  public JmsSender proxyJmsSender() {
    ProxyJmsSender proxyJmsSender = new ProxyJmsSender();
    proxyJmsSender.setWrappedSender(secondActiveJmsSender());
    return proxyJmsSender;
  }

  @Bean
  public JmsSender dummyJmsSender() {
    return new DummyJmsSender();
  }

  @Bean
  public JmsLifecycle filterMessageSender() {
    return new ActiveFilterSender(configurationController, sourceUpdateJmsTemplate);
  }
}
