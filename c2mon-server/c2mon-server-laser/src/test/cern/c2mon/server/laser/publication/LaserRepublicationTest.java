package cern.c2mon.server.laser.publication;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManager;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.test.CacheObjectCreation;

/**
 * Tests on re-publication of failed published alarms in LaserPublisherImpl.
 * 
 * @author Mark Brightwell
 *
 */
public class LaserRepublicationTest {

  IMocksControl mockControl = EasyMock.createNiceControl();
  CacheRegistrationService registrationService;
  AlarmCache alarmCache;
  BatchPersistenceManager alarmPersistenceManager;
  LaserPublisherImpl publisher;
  Lifecycle listenerContainer;
  
  public LaserRepublicationTest(){
    System.setProperty("log4j.configuration",System.getProperty("log4j.configuration", "cern/c2mon/server/laser/publication/log4j.properties"));
    
// IMPORTNANT 
// --- we use the laser test system when submitting an alarm.
    System.setProperty("laser.hosts", "laser-test");
    System.setProperty("cmw.mom.brokerlist", "jms-diamon-test:2506");
// ---
    
    
  }

  @Before
  public void beforeTest() {
    System.setProperty("laser.hosts", "laser-test");
    System.setProperty("cmw.mom.brokerlist", "jms-diamon-test:2506");
    mockControl.reset();    
    registrationService = mockControl.createMock(CacheRegistrationService.class);
    alarmCache = mockControl.createMock(AlarmCache.class);
    listenerContainer = mockControl.createMock(Lifecycle.class);
    alarmPersistenceManager = mockControl.createMock(BatchPersistenceManager.class);    
  }
  
  /**
   * With no successful connection, alarms should end up in re-publication list.
   * Can only be run on its own, as LASER lib does not allow resetting of hosts & brokers in same JVM it seems...
   * 
   * @throws Exception
   */
  //@Test
  public void testRepublicationListOnFailedInit() throws Exception {        
    //reset properties so publication fails
    System.setProperty("laser.hosts", "non-existent");
    System.setProperty("cmw.mom.brokerlist", "non-existent:2506");
    
    ClusterCache clusterCache = mockControl.createMock(ClusterCache.class);
    
    publisher = new LaserPublisherImpl(registrationService, alarmCache, alarmPersistenceManager, clusterCache);
    
    Alarm alarmMock = mockControl.createMock(Alarm.class);
    expect(alarmMock.getId()).andReturn(10L).times(3);
    expect(registrationService.registerToAlarms(publisher)).andReturn(listenerContainer);
    listenerContainer.start();      
    
    mockControl.replay();
    
    publisher.setSourceName("TEST-SOURCE");
    publisher.init();
    publisher.start();
    
    Thread.sleep(1000);
    
    publisher.notifyElementUpdated(alarmMock);
    
    assertTrue(publisher.hasUnpublishedAlarms());    
    mockControl.verify();
  }
  
  /**
   * Fake an exception thrown by LASER API by using mock persistence manager.
   * Republication should then take place and be successful on second attempt
   * (needs small hack of resetting alarmcacheobject between publications so that
   * the alarm publication fields are set to "not published", since the publication
   * was in fact successfull... would of course be better to mock the LASER API and
   * inject using Spring...)
   * @throws Exception 
   */
  @Test 
  public void testRepublicationAfterFailure() throws Exception {
    
    ClusterCache clusterCache = mockControl.createMock(ClusterCache.class);

    publisher = new LaserPublisherImpl(registrationService, alarmCache, alarmPersistenceManager, clusterCache);
    
    expect(registrationService.registerToAlarms(publisher)).andReturn(listenerContainer);
    listenerContainer.start();  
    //listenerContainer.stop();    
    
    Alarm alarmMock = mockControl.createMock(Alarm.class);
    Alarm alarmInCache = CacheObjectCreation.createTestAlarm1();
    assertFalse(alarmInCache.isPublishedToLaser());  //not published   
    Alarm freshAlarmInCache = CacheObjectCreation.createTestAlarm1(); //used to fake a non-publication
    assertFalse(alarmInCache.isPublishedToLaser());  //not published 
    
    expect(alarmMock.getId()).andReturn(alarmInCache.getId()).times(3);    
    expect(alarmCache.get(alarmInCache.getId())).andReturn(alarmInCache);
    expect(alarmCache.get(alarmInCache.getId())).andReturn(freshAlarmInCache);
    expect(alarmCache.getCopy(alarmInCache.getId())).andReturn(freshAlarmInCache);
    alarmPersistenceManager.addElementToPersist(alarmInCache.getId());
    EasyMock.expectLastCall().andThrow(new RuntimeException("test exception"));
    alarmPersistenceManager.addElementToPersist(freshAlarmInCache.getId());    
    
    mockControl.replay();
        
    publisher.setSourceName("TEST-SOURCE");  
    publisher.setRepublishDelay(10000);
    publisher.init();
    publisher.start();    
    
    //wait as LASER connection done in separate thread
    Thread.sleep(4000);
    publisher.notifyElementUpdated(alarmMock);
    
    //now has unpublished alarms
    assertTrue(publisher.hasUnpublishedAlarms());          
    
    //successful after wait
    Thread.sleep(10000);       
    mockControl.verify();
    assertFalse(publisher.hasUnpublishedAlarms());
    
    //check last published value is newly set correctly
    assertTrue(freshAlarmInCache.getLastPublication() != null);
    assertEquals(CacheObjectCreation.createTestAlarm1().getInfo(), freshAlarmInCache.getLastPublication().getInfo());
    assertEquals(CacheObjectCreation.createTestAlarm1().getState(), freshAlarmInCache.getLastPublication().getState());
    assertNotNull(freshAlarmInCache.getLastPublication().getPublicationTime());    
  }
  
}
