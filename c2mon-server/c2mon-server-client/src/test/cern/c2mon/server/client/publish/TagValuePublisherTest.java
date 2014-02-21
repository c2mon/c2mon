package cern.c2mon.server.client.publish;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQTopic;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.alarm.AlarmAggregator;
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
@ContextConfiguration({"classpath:cern/c2mon/server/client/config/server-client-tagpublisher-test.xml" })
public class TagValuePublisherTest {

  private static TestBrokerService testBrokerService = new TestBrokerService();
  
  private IMocksControl control = EasyMock.createNiceControl();
  
  /**
   * To test.
   */
  private TagValuePublisher tagValuePublisher;
  
  /**
   * Mocks of other modules.
   */
  private AlarmAggregator alarmAggregator;  
  private ConfigurationUpdate configurationUpdate;
  private TagFacadeGateway tagFacadeGateway;
  private TagLocationService tagLocationService;

  /**
   * Instantiated in XML.
   */
  @Autowired
  private JmsSender jmsSender;
  
  /**
   * Used for holding update received in test with lock.
   */
  private TagValueUpdate update;
  private TagUpdate updateFromConfig;
  private Object updateLock = new Object();
  
  @BeforeClass
  public static void startBroker() throws Exception {
    testBrokerService.createAndStartBroker();
  }
  
  @Before
  public void setUp() {    
    this.alarmAggregator = control.createMock(AlarmAggregator.class);  
    this.configurationUpdate =  control.createMock(ConfigurationUpdate.class);
    this.tagFacadeGateway = this.control.createMock(TagFacadeGateway.class);
    this.tagLocationService = this.control.createMock(TagLocationService.class);
    //alarmAggregator.registerForTagUpdates(tagValuePublisher);
    this.tagValuePublisher = new TagValuePublisher(this.jmsSender, this.alarmAggregator, this.configurationUpdate, this.tagFacadeGateway, this.tagLocationService);
    this.tagValuePublisher.setRepublicationDelay(1000);
    this.tagValuePublisher.init();
  }
  
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
  public void testPublication() throws JMSException, InterruptedException {
    this.control.reset();
    this.update = null; //make sure update is null before testing
    final DataTag tag = CacheObjectCreation.createTestDataTag3();
    List<Alarm> alarms = new ArrayList<Alarm>();
    alarms.add(CacheObjectCreation.createTestAlarm1()); //attached to this tag
    alarms.add(CacheObjectCreation.createTestAlarm3()); //attached to this tag
    
    Thread listenerThread = startListenerThread(tag);
    
    this.control.replay();    
    
    this.tagValuePublisher.notifyOnUpdate(tag, alarms);
    
    listenerThread.join(1000);
    
    compareTagAndUpdate(tag, alarms, this.update);
    
    this.control.verify();
  }
  
  @Test
  public void testPublicationConfigUpdate() throws JMSException, InterruptedException {
    this.control.reset();
    this.updateFromConfig = null; //make sure update is null before testing
    final DataTag tag = CacheObjectCreation.createTestDataTag3();
    List<Alarm> alarms = new ArrayList<Alarm>();
    alarms.add(CacheObjectCreation.createTestAlarm1()); //attached to this tag
    alarms.add(CacheObjectCreation.createTestAlarm3()); //attached to this tag
    
    TagWithAlarms tagWithAlarms = new TagWithAlarmsImpl(tag, alarms);
    EasyMock.expect(this.tagFacadeGateway.getTagWithAlarms(tag.getId())).andReturn(tagWithAlarms);
    
    
    Thread listenerThread = startListenerThreadForTransferTag(tag);
    
    this.control.replay();    
    
    this.tagValuePublisher.notifyOnConfigurationUpdate(tag.getId());
    
    listenerThread.join(1000);
    
    compareTagAndUpdate(tag, alarms, this.updateFromConfig);
    
    this.control.verify();
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
      assertEquals(alarms.size(), update.getAlarms().size());
      assertEquals(tag.getDescription(), update.getDescription());
      assertEquals(tag.getValueDescription(), update.getValueDescription());
      assertEquals(tag.getCacheTimestamp(), update.getServerTimestamp());
      assertEquals(tag.getSourceTimestamp(), update.getSourceTimestamp());
      assertEquals(tag.getDataTagQuality().getInvalidQualityStates(), update.getDataTagQuality().getInvalidQualityStates()); //compares hashmaps
      assertEquals(tag.getDataTagQuality().getDescription(), update.getDataTagQuality().getDescription());
      assertEquals(TagMode.TEST, update.getMode()); //in DataTagCacheObject is old constant 2, which indicates mode test
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
          JmsTemplate template = new JmsTemplate(testBrokerService.getConnectionFactory());
          template.setReceiveTimeout(1000);
          Message message = template.receive(new ActiveMQTopic(tag.getTopic()));                   
          synchronized (updateLock) {
            update = TransferTagValueImpl.fromJson(((TextMessage) message).getText());
          }                                  
        } catch (Exception e) {          
          update = null;          
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
          JmsTemplate template = new JmsTemplate(testBrokerService.getConnectionFactory());
          template.setReceiveTimeout(1000);
          Message message = template.receive(new ActiveMQTopic(tag.getTopic()));                   
          synchronized (updateLock) {
            updateFromConfig = TransferTagImpl.fromJson(((TextMessage) message).getText());
          }                                  
        } catch (Exception e) {          
          updateFromConfig = null;          
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
  public void testRepublication() throws Exception {
    this.control.reset();
    this.update = null;
    testBrokerService.stopBroker();
    
    //try publication
    final DataTag tag = CacheObjectCreation.createTestDataTag3();
    List<Alarm> alarms = new ArrayList<Alarm>();
    Alarm alarm1 = CacheObjectCreation.createTestAlarm1();
    Alarm alarm2 = CacheObjectCreation.createTestAlarm3(); 
    alarms.add(alarm1); //attached to this tag
    alarms.add(alarm2); //attached to this tag
    
    Thread listenerThread = startListenerThread(tag); //will throw exception
    
    this.control.replay();
    tagValuePublisher.notifyOnUpdate(tag, alarms);
    listenerThread.join(1000); //will fail after 100ms (failover timeout)
    assertTrue(this.update == null); //update failed as broker stopped
   
    Thread.sleep(1000); //allow another republication to fail (after 1s=republication delay)
                        //then start broker & listener before next republication attempt!
    testBrokerService.createAndStartBroker();  //connection to listener thread must have time to establish itself before republication
    listenerThread = startListenerThread(tag); //new thread     
    
    listenerThread.join(1000);
    
    compareTagAndUpdate(tag, alarms, this.update);
    
    this.control.verify();    
  }
  
  @Test
  public void testRepublicationConfigUpdate() throws Exception {
    this.control.reset();
    this.updateFromConfig = null;
    testBrokerService.stopBroker();
    
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
    
    this.control.replay();
    tagValuePublisher.notifyOnConfigurationUpdate(tag.getId());
    listenerThread.join(1000); //will fail after 100ms (failover timeout)
    assertTrue(this.updateFromConfig == null); //update failed as broker stopped
   
    Thread.sleep(1000); //allow another republication to fail (after 1s=republication delay)
                        //then start broker & listener before next republication attempt!
    testBrokerService.createAndStartBroker();  //connection to listener thread must have time to establish itself before republication
    listenerThread = startListenerThreadForTransferTag(tag); //new thread     
    
    listenerThread.join(1000);
    
    compareTagAndUpdate(tag, alarms, this.updateFromConfig);
    
    this.control.verify();    
  }
  
  @AfterClass
  public static void stopBroker() throws Exception {
    testBrokerService.stopBroker();
  }
  
}
