package cern.c2mon.server.laser.publication;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import cern.laser.source.alarmsysteminterface.ASIException;
import cern.laser.source.alarmsysteminterface.AlarmSystemInterface;
import cern.tim.server.cache.AlarmCache;
import cern.tim.server.common.alarm.Alarm;
import cern.tim.server.test.CacheObjectCreation;

/**
 * Unit test of LaserBackupPublisher.
 * 
 * @author Mark Brightwell
 *
 */
public class LaserBackupPublisherTest {

  private LaserBackupPublisher laserBackupPublisher;
  
  private AlarmCache alarmCache;
  
  private LaserPublisher laserPublisher;
  
  private IMocksControl controller;
  
  private AlarmSystemInterface asi;
  
  private ReentrantReadWriteLock backupLock = new ReentrantReadWriteLock();
  
  public LaserBackupPublisherTest() {
    super();
    System.setProperty("log4j.configuration",System.getProperty("log4j.configuration", "cern/c2mon/server/laser/publication/log4j.properties"));
 // IMPORTNANT 
 // --- we use the laser test system when submitting an alarm.
     System.setProperty("laser.hosts", "laser-test");
     System.setProperty("cmw.mom.brokerlist", "jms-diamon-test:2506");
 // ---
  }

  @Before
  public void beforeTest() {
    controller = EasyMock.createNiceControl();
    alarmCache = controller.createMock(AlarmCache.class);
    laserPublisher = controller.createMock(LaserPublisher.class);
    asi = controller.createMock(AlarmSystemInterface.class);
    laserBackupPublisher = new LaserBackupPublisher(alarmCache, laserPublisher);
    laserBackupPublisher.setBackupInterval(60000);
  }
  
  @Test
  public void testBackup() throws InterruptedException, ASIException {  
    Alarm alarm1 = CacheObjectCreation.createTestAlarm1();
    Alarm alarm2 = CacheObjectCreation.createTestAlarm2();    
    EasyMock.expect(laserPublisher.getAsi()).andReturn(asi).times(2);
    asi.pushActiveList(EasyMock.isA(List.class));
    EasyMock.expect(laserPublisher.getBackupLock()).andReturn(backupLock).times(2);
    EasyMock.expect(alarmCache.getKeys()).andReturn(Arrays.asList(alarm1.getId(), alarm2.getId()));
    EasyMock.expect(alarmCache.getCopy(alarm1.getId())).andReturn(alarm1);
    EasyMock.expect(alarmCache.getCopy(alarm2.getId())).andReturn(alarm2);
    
    controller.replay();
    laserBackupPublisher.start(); //sets state to running and starts thread (not needed for test)
    Thread.sleep(3000);
    laserBackupPublisher.run(); //call run directly
    controller.verify();
  }
  
}
