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

import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.client.tag.TransferTagValueImpl;
import cern.tim.server.alarm.AlarmAggregator;
import cern.tim.server.cache.AlarmCache;
import cern.tim.server.cache.TagLocationService;
import cern.tim.server.common.alarm.Alarm;
import cern.tim.server.common.datatag.DataTag;
import cern.tim.server.common.tag.Tag;
import cern.tim.server.test.CacheObjectCreation;
import cern.tim.server.test.broker.TestBrokerService;
import cern.tim.util.jms.JmsSender;

/**
 * Integration test of TagValuePublisher with broker.
 * Tests publication & re-publication works. 
 * 
 * @author Mark Brightwell
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
  private TagLocationService tagLocationService;
  private AlarmCache alarmCache;
  
  /**
   * Instantiated in XML.
   */
  @Autowired
  private JmsSender jmsSender;
  
  /**
   * Used for holding update received in test with lock.
   */
  private TagValueUpdate update;
  private Object updateLock = new Object();
  
  @BeforeClass
  public static void startBroker() throws Exception {
    testBrokerService.createAndStartBroker();
  }
  
  @Before
  public void setUp() {    
    alarmAggregator = control.createMock(AlarmAggregator.class);
    tagLocationService = control.createMock(TagLocationService.class);
    alarmCache = control.createMock(AlarmCache.class);
    //alarmAggregator.registerForTagUpdates(tagValuePublisher);
    tagValuePublisher = new TagValuePublisher(jmsSender, alarmAggregator, tagLocationService, alarmCache);
    tagValuePublisher.setRepublicationDelay(100);
    tagValuePublisher.init();
  }
  
  @After
  public void afterTest(){
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
    control.reset();
    update = null; //make sure update is null before testing
    final DataTag tag = CacheObjectCreation.createTestDataTag3();
    List<Alarm> alarms = new ArrayList<Alarm>();
    alarms.add(CacheObjectCreation.createTestAlarm1()); //attached to this tag
    alarms.add(CacheObjectCreation.createTestAlarm3()); //attached to this tag
    
    Thread listenerThread = startListenerThread(tag);
    
    control.replay();    
    
    tagValuePublisher.notifyOnUpdate(tag, alarms);
    
    listenerThread.join(1000);
    
    compareTagAndUpdate(tag, alarms, update);
    
    control.verify();
  }
  
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
  
  private Thread startListenerThread(final Tag tag) {
    // start listener in separate thread (to catch update to topic)
    Thread listenerThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        try {
          JmsTemplate template = new JmsTemplate(testBrokerService.getConnectionFactory());
          template.setReceiveTimeout(500);
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
   * Tests behaviour when publication fails when broker is down, and that re-publication occurs
   * when the broker is restarted. Uses 2 successive listener threads to check if publication occurred:
   * the first one fails (update is null), the second succeeds (after the broker is restarted).
   * @throws Exception 
   */
  @Test
  public void testRepublication() throws Exception {
    control.reset();
    update = null;
    testBrokerService.stopBroker();
    
    //try publication
    final DataTag tag = CacheObjectCreation.createTestDataTag3();
    List<Alarm> alarms = new ArrayList<Alarm>();
    Alarm alarm1 = CacheObjectCreation.createTestAlarm1();
    Alarm alarm2 = CacheObjectCreation.createTestAlarm3(); 
    alarms.add(alarm1); //attached to this tag
    alarms.add(alarm2); //attached to this tag
    
    Thread listenerThread = startListenerThread(tag); //will throw exception
    
    control.replay();
    tagValuePublisher.notifyOnUpdate(tag, alarms);
    listenerThread.join(1000);
    assertTrue(update == null); //update failed as broker stopped
   
    Thread.sleep(1000); //allow a number of republications to fail
    listenerThread = startListenerThread(tag); //new thread
    testBrokerService.createAndStartBroker(); //only start broker once listener thread is running, or will miss the re-publication!      
    
    listenerThread.join(1000);
    
    compareTagAndUpdate(tag, alarms, update);
    
    control.verify();    
  }
  
  @AfterClass
  public static void stopBroker() throws Exception {
    testBrokerService.stopBroker();
  }
  
}
