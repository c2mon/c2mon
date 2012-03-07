/**
 * 
 */
package cern.c2mon.server.laser.publication;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import oracle.net.aso.p;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import cern.tim.server.cache.AlarmCache;
import cern.tim.server.cache.CacheRegistrationService;
import cern.tim.server.cachepersistence.common.BatchPersistenceManager;
import cern.tim.server.common.alarm.Alarm;
import cern.tim.server.test.CacheObjectCreation;

/**
 * Component test of LASER module (& integration test
 * with LASER API).
 * 
 * @author felixehm, Mark Brightwell
 *
 */
public class LaserAlarmPublisherImplTest {

	IMocksControl mockControl = EasyMock.createNiceControl();
	CacheRegistrationService registrationService;
	AlarmCache alarmCache;
	BatchPersistenceManager alarmPersistenceManager;
	LaserPublisherImpl publisher;
	public LaserAlarmPublisherImplTest(){
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
    alarmPersistenceManager = mockControl.createMock(BatchPersistenceManager.class);
    publisher = new LaserPublisherImpl(registrationService, alarmCache, alarmPersistenceManager);
	}
	
	@Test
	public void testSendActiveAlarmFaultState() throws InterruptedException {
		
		registrationService.registerToAlarms(publisher);		
		
		Alarm alarmMock = mockControl.createMock(Alarm.class);
		Alarm alarmInCache = CacheObjectCreation.createTestAlarm2();
		assertTrue(alarmInCache.isPublishedToLaser()); //published value set in test object
		
		expect(alarmMock.getId()).andReturn(alarmInCache.getId());
		expect(alarmCache.get(alarmInCache.getId())).andReturn(alarmInCache);
		alarmPersistenceManager.addElementToPersist(alarmInCache.getId());
		mockControl.replay();
				
		publisher.setSourceName("TEST-SOURCE");
		try {
			publisher.init();
			publisher.start();		
		} catch (Exception e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
		//wait as LASER connection done in separate thread
		Thread.sleep(10000);
		publisher.notifyElementUpdated(alarmMock);
		mockControl.verify();
		
		//check last published value is newly set correctly
		assertTrue(alarmInCache.getLastPublication() != null);
		assertEquals(alarmInCache.getLastPublication().getInfo(), CacheObjectCreation.createTestAlarm2().getInfo());
		assertEquals(alarmInCache.getLastPublication().getState(), CacheObjectCreation.createTestAlarm2().getState());
		assertNotNull(alarmInCache.getLastPublication().getPublicationTime());
	}
	
	
	@Test
	public void testSendTerninatedAlarmFaultState() throws InterruptedException {
	  
	  registrationService.registerToAlarms(publisher);    
    
    Alarm alarmMock = mockControl.createMock(Alarm.class);
    Alarm alarmInCache = CacheObjectCreation.createTestAlarm1();
    assertTrue(alarmInCache.isPublishedToLaser()); //published value set in test object
    
    expect(alarmMock.getId()).andReturn(alarmInCache.getId());
    expect(alarmCache.get(alarmInCache.getId())).andReturn(alarmInCache);
    alarmPersistenceManager.addElementToPersist(alarmInCache.getId());
    mockControl.replay();
        
    publisher.setSourceName("TEST-SOURCE");
    try {
      publisher.init();
      publisher.start();    
    } catch (Exception e) {
      fail(e.getMessage());
      e.printStackTrace();
    }
    //wait as LASER connection done in separate thread
    Thread.sleep(5000);
    publisher.notifyElementUpdated(alarmMock);
    mockControl.verify();
    
    //check last published value is newly set correctly
    assertTrue(alarmInCache.getLastPublication() != null);
    assertEquals(alarmInCache.getLastPublication().getInfo(), CacheObjectCreation.createTestAlarm1().getInfo());
    assertEquals(alarmInCache.getLastPublication().getState(), CacheObjectCreation.createTestAlarm1().getState());
    assertNotNull(alarmInCache.getLastPublication().getPublicationTime());    
	}
	
	/**
	 * With no successful connection, alarms should end up in re-publication list.
	 * @throws Exception
	 */
	@Test
	public void testRepublicationListOnFailedInit() throws Exception {	  	  
	  //reset properties so publication fails
	  System.setProperty("laser.hosts", "non-existent");
    System.setProperty("cmw.mom.brokerlist", "non-existent:2506");
    
    Alarm alarmMock = mockControl.createMock(Alarm.class);
    expect(alarmMock.getId()).andReturn(10L).times(3);
    
    mockControl.replay();
    
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
	  
	  registrationService.registerToAlarms(publisher);    
    
    Alarm alarmMock = mockControl.createMock(Alarm.class);
    Alarm alarmInCache = CacheObjectCreation.createTestAlarm1();
    assertTrue(alarmInCache.isPublishedToLaser()); //published value set in test object
    
    expect(alarmMock.getId()).andReturn(alarmInCache.getId());    
    expect(alarmCache.get(alarmInCache.getId())).andReturn(alarmInCache).times(2);
    expect(alarmCache.getCopy(alarmInCache.getId())).andReturn(alarmInCache);
    alarmPersistenceManager.addElementToPersist(alarmInCache.getId());
    EasyMock.expectLastCall().andThrow(new RuntimeException("test exception"));
    alarmPersistenceManager.addElementToPersist(alarmInCache.getId());
    
    
    mockControl.replay();
        
    publisher.setSourceName("TEST-SOURCE");  
    publisher.setRepublishDelay(8000);
    publisher.init();
    publisher.start();    
    
    //wait as LASER connection done in separate thread
    Thread.sleep(5000);
    publisher.notifyElementUpdated(alarmMock);
    
    //now has unpublished alarms
    assertTrue(publisher.hasUnpublishedAlarms());
    //reset alarmCacheObject as was actually successfull but want to check republication occurred (filtered out otherwise)
    alarmInCache = CacheObjectCreation.createTestAlarm1();
    
    //successful after wait
    Thread.sleep(5000);
    assertFalse(publisher.hasUnpublishedAlarms());
    
    //check last published value is newly set correctly
    assertTrue(alarmInCache.getLastPublication() != null);
    assertEquals(alarmInCache.getLastPublication().getInfo(), CacheObjectCreation.createTestAlarm1().getInfo());
    assertEquals(alarmInCache.getLastPublication().getState(), CacheObjectCreation.createTestAlarm1().getState());
    assertNotNull(alarmInCache.getLastPublication().getPublicationTime());  
    mockControl.verify();
	}
	
	@Test
	public void testStopStart() throws Exception {
	  publisher.setSourceName("TEST-SOURCE");
	  publisher.init();
	  assertFalse(publisher.isRunning());
	  publisher.start();
	  assertTrue(publisher.isRunning());
	  publisher.stop();
	  assertFalse(publisher.isRunning());
	  publisher.start();
	  publisher.stop();
	}
	
}
