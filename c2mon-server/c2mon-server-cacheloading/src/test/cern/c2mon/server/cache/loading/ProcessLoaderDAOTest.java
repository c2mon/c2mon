package cern.c2mon.server.cache.loading;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.common.process.Process;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration({"classpath:cern/c2mon/server/cache/loading/config/server-cacheloading-process-test.xml"})
public class ProcessLoaderDAOTest {

  @Autowired
  private ProcessDAO processDAO;
  
  @Value("${c2mon.jms.daq.queue.trunk}") 
  private String jmsDaqQueueTrunk;
  
  @Test
  public void testGetItem() {
    assertNotNull(processDAO.getItem(50L));
  }
  
  /**
   * Check the default property is picked up (should override that set in the
   * cache object itself).
   */
  @Test
  public void testGetItemDoPostDbLoading() {
    Process process = processDAO.getItem(51L);
    assertNotNull(process);
    assertTopicSetCorrectly(process);
  }
  
  @Test
  public void testGetAll() {
    assertNotNull(processDAO.getAllAsMap());
    assertEquals(2, processDAO.getAllAsMap().size());
  }
  
  @Test
  public void testGetAllDoPostDbLoading() {
    for (Map.Entry<Long, Process> entry : processDAO.getAllAsMap().entrySet()) {
      assertTopicSetCorrectly(entry.getValue());
    }
  }
  
  private void assertTopicSetCorrectly(Process process) {
    assertEquals(this.jmsDaqQueueTrunk + ".command." + process.getCurrentHost() + "." + process.getName() + "." + process.getProcessPIK(), process.getJmsDaqCommandQueue());
  }
  
}
