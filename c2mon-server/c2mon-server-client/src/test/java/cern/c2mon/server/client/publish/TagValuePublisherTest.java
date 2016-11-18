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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import cern.c2mon.server.client.junit.CachePopulationRule;
import cern.c2mon.shared.client.serializer.TransferTagSerializer;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.BrokerServiceAware;
import org.apache.activemq.command.ActiveMQTopic;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.alarm.AlarmAggregator;
import cern.c2mon.server.cache.AliveTimerFacade;
import cern.c2mon.server.cache.TagFacadeGateway;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.alarm.TagWithAlarmsImpl;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.configuration.ConfigurationUpdate;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.server.test.broker.TestBrokerService;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.client.tag.TransferTagValueImpl;
import cern.c2mon.shared.util.jms.JmsSender;

/**
 * Integration test of TagValuePublisher with broker.
 * Tests publication & re-publication works.
 *
 * @author Mark Brightwell, Ignacio Vilches
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
    "classpath:config/server-client.xml",
    "classpath:config/server-cache.xml",
    "classpath:config/server-cachedbaccess.xml",
    "classpath:config/server-configuration.xml",
    "classpath:config/server-daqcommunication-in.xml",
    "classpath:config/server-daqcommunication-out.xml",
    "classpath:config/server-rule.xml",
    "classpath:config/server-supervision.xml",
    "classpath:config/server-alarm.xml",
    "classpath:config/server-command.xml",
    "classpath:test-config/server-test-properties.xml"
})
@TestPropertySource("classpath:c2mon-server-default.properties")
public class TagValuePublisherTest {

  @Rule
  @Autowired
  public CachePopulationRule cachePopulationRule;

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

  /**
   * Used for holding update received in test with lock.
   */
  private TagValueUpdate update;
  private TagUpdate updateFromConfig;
  private Object updateLock = new Object();

  @After
  public void afterTest() {
    tagValuePublisher.shutdown();
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
    this.tagValuePublisher.notifyOnUpdate(tag, alarms);

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
            Message message = template.receive(new ActiveMQTopic(tag.getTopic()));
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
            Message message = template.receive(new ActiveMQTopic(tag.getTopic()));
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
    tagValuePublisher.notifyOnUpdate(tag, alarms);
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
