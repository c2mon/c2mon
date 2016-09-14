/******************************************************************************
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
 *****************************************************************************/
package cern.c2mon.server.client.publish;

import cern.c2mon.server.alarm.config.AlarmModule;
import cern.c2mon.server.cache.TagFacadeGateway;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.cache.config.CacheModule;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.client.config.ClientModule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.client.junit.ClientCachePopulationRule;
import cern.c2mon.server.command.config.CommandModule;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.alarm.TagWithAlarmsImpl;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.configuration.config.ConfigurationModule;
import cern.c2mon.server.daq.config.DaqModule;
import cern.c2mon.server.daq.update.JmsContainerManagerImpl;
import cern.c2mon.server.rule.config.RuleModule;
import cern.c2mon.server.supervision.config.SupervisionModule;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.server.test.broker.EmbeddedBrokerRule;
import cern.c2mon.server.test.config.TestConfig;
import cern.c2mon.shared.client.serializer.TransferTagSerializer;
import cern.c2mon.shared.client.tag.*;
import org.apache.activemq.command.ActiveMQTopic;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration test of TagValuePublisher with broker.
 * Tests publication & re-publication works.
 *
 * @author Mark Brightwell, Ignacio Vilches
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    CommonModule.class,
    CacheModule.class,
    CacheDbAccessModule.class,
    SupervisionModule.class,
    ConfigurationModule.class,
    DaqModule.class,
    RuleModule.class,
    ClientModule.class,
    AlarmModule.class,
    CommandModule.class,
    TestConfig.class
})
public class TagValuePublisherTest {

  @Rule
  @Autowired
  public ClientCachePopulationRule clientCachePopulationRule;

  @Rule
  @Autowired
  public EmbeddedBrokerRule brokerRule;

  /**
   * To test.
   */
  @Autowired
  private TagValuePublisher tagValuePublisher;

  @Autowired
  private TagLocationService tagLocationService;

  @Autowired
  private TagFacadeGateway tagFacadeGateway;

  @Autowired
  @Qualifier("clientActiveMQConnectionFactory")
  private ConnectionFactory connectionFactory;

  @Autowired
  private JmsContainerManagerImpl jmsContainerManager;

  /**
   * Used for holding update received in test with lock.
   */
  private TagValueUpdate update;
  private TagUpdate updateFromConfig;
  private Object updateLock = new Object();

  @After
  public void afterTest() {
    tagValuePublisher.shutdown();
    jmsContainerManager.stop();
  }

  /**
   * Tests the value is indeed published to the broker and the update
   * is correctly transmitted to the broker.
   * @throws JMSException
   * @throws InterruptedException
   */
  @Test
  @Ignore("This test is flaky, maybe due to all the sleep() calls")
  public void testPublication() throws JMSException, InterruptedException {
    EasyMock.reset();
    synchronized (updateLock) {
      this.update = null; //make sure update is null before testing
    }
    final DataTag tag = (DataTag) tagLocationService.get(200000L);
    List<Alarm> alarms = new ArrayList<Alarm>();
    alarms.add(CacheObjectCreation.createTestAlarm1()); //attached to this tag
    alarms.add(CacheObjectCreation.createTestAlarm3()); //attached to this tag

    Thread listenerThread = startListenerThread(tag);

    EasyMock.replay();
    Thread.sleep(500);
    this.tagValuePublisher.notifyElementUpdated(tag);

    listenerThread.join(1000);

    compareTagAndUpdate(tag, alarms, this.update);

    EasyMock.verify();
  }

  @Test
  @Ignore("This test is flaky, maybe due to all the sleep() calls")
  public void testPublicationConfigUpdate() throws JMSException, InterruptedException {
//    EasyMock.reset();
    synchronized (updateLock) {
      this.updateFromConfig = null; //make sure update is null before testing
    }
    final DataTag tag = (DataTag) tagLocationService.get(200000L);
    List<Alarm> alarms = new ArrayList<Alarm>();
    alarms.add(CacheObjectCreation.createTestAlarm1()); //attached to this tag
    alarms.add(CacheObjectCreation.createTestAlarm3()); //attached to this tag

//    TagWithAlarms tagWithAlarms = new TagWithAlarmsImpl(tag, alarms);
//    EasyMock.expect(this.tagFacadeGateway.getTagWithAlarms(tag.getId())).andReturn(tagWithAlarms);


    Thread listenerThread = startListenerThreadForTransferTag(tag);

//    EasyMock.replay();
    Thread.sleep(500);
    this.tagValuePublisher.notifyOnConfigurationUpdate(tag.getId());

    listenerThread.join(1000);

    compareTagAndUpdate(tag, alarms, this.updateFromConfig);

//    EasyMock.verify();
  }

  /**
   *
   * @param tag
   * @param alarms
   * @param update
   */
  private void compareTagAndUpdate(DataTag tag, List<Alarm> alarms, TagValueUpdate update) {
    synchronized (updateLock) {
      assertNotNull(update); //message or update were null
      assertEquals(tag.getValue(), update.getValue());
      assertEquals(tag.getId(), update.getId());
//      assertEquals(alarms.size(), update.getAlarms().size());
      assertEquals(tag.getDescription(), update.getDescription());
      assertEquals(tag.getValueDescription(), update.getValueDescription());
      assertEquals(tag.getCacheTimestamp(), update.getServerTimestamp());
//      assertEquals(tag.getSourceTimestamp(), update.getSourceTimestamp());
      assertEquals(tag.getDataTagQuality().getInvalidQualityStates(), update.getDataTagQuality().getInvalidQualityStates()); //compares hashmaps
      assertEquals(tag.getDataTagQuality().getDescription(), update.getDataTagQuality().getDescription());
      assertEquals(TagMode.OPERATIONAL, update.getMode()); //in DataTagCacheObject is old constant 2, which indicates mode test
    }
  }

  /**
   * Listens for 1s for updates on the tag topic.
   */
  private Thread startListenerThread(final Tag tag) {
    // start listener in separate thread (to catch update to topic)
    Thread listenerThread = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          JmsTemplate template = new JmsTemplate(connectionFactory);
          template.setReceiveTimeout(1000);
          synchronized (updateLock) {
            Message message = template.receive(new ActiveMQTopic("c2mon.client.tag." + ((DataTag) tag).getProcessId()));
            update = TransferTagSerializer.fromJson(((TextMessage) message).getText(), TransferTagValueImpl.class);
          }
        } catch (Exception e) {
          synchronized (updateLock) {
            update = null;
          }
        }
      }
    });
    listenerThread.start();
    return listenerThread;
  }

  /**
   * Listens for 1s for updates on the tag topic.
   */
  private Thread startListenerThreadForTransferTag(final Tag tag) {
    // start listener in separate thread (to catch update to topic)
    Thread listenerThread = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          JmsTemplate template = new JmsTemplate(connectionFactory);
          template.setReceiveTimeout(5000);
          synchronized (updateLock) {
            Message message = template.receive(new ActiveMQTopic("c2mon.client.tag." + ((DataTag) tag).getProcessId()));
            updateFromConfig = TransferTagSerializer.fromJson(((TextMessage) message).getText(), TransferTagImpl.class);
          }
        } catch (Exception e) {
          synchronized (updateLock) {
            updateFromConfig = null;
          }
        }
      }
    });
    listenerThread.start();
    return listenerThread;
  }

  /**
   * Tests behaviour when publication fails when broker is down, and that re-publication occurs
   * when the broker is restarted. Uses 2 successive listener threads to check if publication occurred:
   * the first one fails (update is null), the second succeeds (after the broker is restarted).
   * @throws Exception
   */
  @Test
  @Ignore("This test is flaky, maybe due to all the sleep() calls")
  public void testRepublication() throws Exception {
    EasyMock.reset();
    synchronized (updateLock) {
      this.update = null;
    }
//    testBrokerService.stop();

    //try publication
    final DataTag tag = CacheObjectCreation.createTestDataTag3();
    List<Alarm> alarms = new ArrayList<Alarm>();
    Alarm alarm1 = CacheObjectCreation.createTestAlarm1();
    Alarm alarm2 = CacheObjectCreation.createTestAlarm3();
    alarms.add(alarm1); //attached to this tag
    alarms.add(alarm2); //attached to this tag

    Thread listenerThread = startListenerThread(tag); //will throw exception

    EasyMock.replay();
    tagValuePublisher.notifyElementUpdated(tag);
    listenerThread.join(1000); //will fail after 100ms (failover timeout)
    synchronized (updateLock) {
      assertTrue(this.update == null); //update failed as broker stopped
    }

    Thread.sleep(2000); //allow another republication to fail (after 1s=republication delay)
                        //then start broker & listener before next republication attempt!
//    testBrokerService.start();  //connection to listener thread must have time to establish itself before republication
    listenerThread = startListenerThread(tag); //new thread

    listenerThread.join(1000);

    compareTagAndUpdate(tag, alarms, this.update);

    EasyMock.verify();
  }

  @Test
  @Ignore("This test is flaky, maybe due to all the sleep() calls")
  public void testRepublicationConfigUpdate() throws Exception {
    EasyMock.reset();
    synchronized (updateLock) {
      this.updateFromConfig = null;
    }
//    testBrokerService.stop();

    //try publication
    final DataTag tag = CacheObjectCreation.createTestDataTag3();
    List<Alarm> alarms = new ArrayList<Alarm>();
    Alarm alarm1 = CacheObjectCreation.createTestAlarm1();
    Alarm alarm2 = CacheObjectCreation.createTestAlarm3();
    alarms.add(alarm1); //attached to this tag
    alarms.add(alarm2); //attached to this tag

    TagWithAlarms tagWithAlarms = new TagWithAlarmsImpl(tag, alarms);
    EasyMock.expect(this.tagFacadeGateway.getTagWithAlarms(tag.getId())).andReturn(tagWithAlarms);

    Thread listenerThread = startListenerThreadForTransferTag(tag); //will throw exception

    EasyMock.replay();
    tagValuePublisher.notifyOnConfigurationUpdate(tag.getId());
    listenerThread.join(1000); //will fail after 100ms (failover timeout)
    synchronized (updateLock) {
      assertTrue(this.updateFromConfig == null); //update failed as broker stopped
    }

    Thread.sleep(1000); //allow another republication to fail (after 1s=republication delay)
                        //then start broker & listener before next republication attempt!
//    testBrokerService.start();  //connection to listener thread must have time to establish itself before republication
    listenerThread = startListenerThreadForTransferTag(tag); //new thread

    listenerThread.join(1000);

    compareTagAndUpdate(tag, alarms, this.updateFromConfig);

    EasyMock.verify();
  }
}
