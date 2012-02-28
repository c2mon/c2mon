/**
 * 
 */
package cern.c2mon.server.laser.publication;



import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.sql.Timestamp;

import javax.validation.constraints.AssertTrue;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import cern.tim.server.cache.AlarmCache;
import cern.tim.server.cache.CacheRegistrationService;
import cern.tim.server.cachepersistence.common.BatchPersistenceManager;
import cern.tim.server.common.alarm.Alarm;
import cern.tim.server.test.CacheObjectCreation;
import ch.cern.tim.shared.alarm.AlarmCondition;

/**
 * @author felixehm
 *
 */
public class LaserAlarmPublisherImplTest {

	IMocksControl mockControl = EasyMock.createControl();
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
    Thread.sleep(10000);
    publisher.notifyElementUpdated(alarmMock);
    mockControl.verify();
    
    //check last published value is newly set correctly
    assertTrue(alarmInCache.getLastPublication() != null);
    assertEquals(alarmInCache.getLastPublication().getInfo(), CacheObjectCreation.createTestAlarm1().getInfo());
    assertEquals(alarmInCache.getLastPublication().getState(), CacheObjectCreation.createTestAlarm1().getState());
    assertNotNull(alarmInCache.getLastPublication().getPublicationTime());    
	}
	
	
}
