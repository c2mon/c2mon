/**
 * 
 */
package cern.c2mon.server.laser.publication;



import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.fail;

import java.sql.Timestamp;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Test;

import cern.tim.server.cache.CacheRegistrationService;
import cern.tim.server.common.alarm.Alarm;
import ch.cern.tim.shared.alarm.AlarmCondition;

/**
 * @author felixehm
 *
 */
public class LaserAlarmPublisherImplTest {

	IMocksControl mockControl = EasyMock.createControl();
	CacheRegistrationService registrationService;
	public LaserAlarmPublisherImplTest(){
		System.setProperty("log4j.configuration",System.getProperty("log4j.configuration", "cern/c2mon/server/laser/publication/log4j.properties"));
		
// IMPORTNANT 
// --- we use the laser test system when submitting an alarm.
		System.setProperty("laser.hosts", "laser-test");
		System.setProperty("cmw.mom.brokerlist", "jms-diamon-test:2506");
// ---
		
		
	}
	
	@Test
	public void testSendActiveAlarmFaultState() {
		
		LaserPublisher publisher = null;
		
		registrationService = createMock(CacheRegistrationService.class);
		//registrationService.registerToAlarms(publisher);
		//expectLastCall().once();
		
		Alarm alarmMock = mockControl.createMock(Alarm.class);
		expect(alarmMock.isActive()).andReturn(Boolean.TRUE);
		expect(alarmMock.getFaultCode()).andReturn(1).anyTimes();
		expect(alarmMock.getFaultFamily()).andReturn("TESTALARM").anyTimes();
		expect(alarmMock.getFaultMember()).andReturn("TEST-MEMBER").anyTimes();
		expect(alarmMock.getTimestamp()).andReturn(new Timestamp(System.currentTimeMillis())).anyTimes();
		expect(alarmMock.getInfo()).andReturn("A Test Alarm").anyTimes();
		expect(alarmMock.getState()).andReturn(AlarmCondition.ACTIVE).anyTimes();
		replay(alarmMock);
		
		publisher = new LaserPublisher(registrationService);
		publisher.setSourceName("TEST-SOURCE");
		try {
			publisher.init();
		} catch (Exception e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
		publisher.notifyElementUpdated(alarmMock);
		verify(alarmMock);
	}
	
	
	@Test
	public void testSendTerninatedAlarmFaultState() {
		
		LaserPublisher publisher = null;
		
		registrationService = createMock(CacheRegistrationService.class);
		//registrationService.registerToAlarms(publisher);
		//expectLastCall().once();
		
		Alarm alarmMock = mockControl.createMock(Alarm.class);
		expect(alarmMock.getFaultCode()).andReturn(1).anyTimes();
		expect(alarmMock.getFaultFamily()).andReturn("TESTALARM").anyTimes();
		expect(alarmMock.getFaultMember()).andReturn("TEST-MEMBER").anyTimes();
		expect(alarmMock.getTimestamp()).andReturn(new Timestamp(System.currentTimeMillis())).anyTimes();
		expect(alarmMock.getInfo()).andReturn("A Test Alarm").anyTimes();
		expect(alarmMock.getState()).andReturn(AlarmCondition.TERMINATE).anyTimes();
		replay(alarmMock);
		
		publisher = new LaserPublisher(registrationService);
		publisher.setSourceName("TEST-SOURCE");
		try {
			publisher.init();
		} catch (Exception e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
		publisher.notifyElementUpdated(alarmMock);
		verify(alarmMock);
	}
	
	
}
