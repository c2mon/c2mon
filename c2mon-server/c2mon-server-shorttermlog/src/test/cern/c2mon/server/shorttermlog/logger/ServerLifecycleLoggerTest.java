package cern.c2mon.server.shorttermlog.logger;

import org.apache.ibatis.exceptions.PersistenceException;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.shared.client.lifecycle.ServerLifecycleEvent;
import cern.c2mon.shared.client.lifecycle.ServerLifecycleMapper;

/**
 * Unit test of ServerLifecycleLogger.
 * 
 * @author Mark Brightwell
 *
 */
public class ServerLifecycleLoggerTest {

  private ServerLifecycleLogger serverLifecycleLogger;
  
  private ServerLifecycleMapper lifecycleMapper;
  
  private IMocksControl control;
  
  @Before
  public void init() {
    control = EasyMock.createNiceControl();
    lifecycleMapper = control.createMock(ServerLifecycleMapper.class);
    serverLifecycleLogger = new ServerLifecycleLogger(lifecycleMapper);
    serverLifecycleLogger.setServerName("test-server");
    serverLifecycleLogger.setTimebetweenRelogs(1000);
  }
  
  /**
   * Tests normal start/stop logging by calling directly the lifecycle
   * methods.
   */
  @Test
  public void testLogStartStop() {
    lifecycleMapper.logEvent(EasyMock.isA(ServerLifecycleEvent.class));
    EasyMock.expectLastCall().times(2);
    control.replay();
    serverLifecycleLogger.start();
    serverLifecycleLogger.stop();
    control.verify();
  }
  
  /**
   * Test the start log is repeated if logging fails.
   * Expect at least 3 tries in 5s. Also checks stop returns.
   * @throws InterruptedException 
   */
  @Test
  public void testRepeatStartLog() throws InterruptedException {
    lifecycleMapper.logEvent(EasyMock.isA(ServerLifecycleEvent.class));
    EasyMock.expectLastCall().andThrow(new PersistenceException()).times(3,10);
    control.replay();
    serverLifecycleLogger.start();
    Thread.sleep(5000);
    serverLifecycleLogger.stop();
    control.verify();    
  }
}
