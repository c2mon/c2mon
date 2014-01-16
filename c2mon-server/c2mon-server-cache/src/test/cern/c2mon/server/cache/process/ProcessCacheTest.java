package cern.c2mon.server.cache.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.dbaccess.ProcessMapper;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.shared.common.Cacheable;

/**
 * Integration test of ProcessCache with loading
 * and DB access cache modules.
 * 
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration({"classpath:cern/c2mon/server/test/cache/config/server-cache-process-test.xml"})
public class ProcessCacheTest {
  
  @Autowired  
  private ProcessMapper processMapper;
  
  @Autowired
  private ProcessCacheImpl processCache;
  
  @Test
  public void testCacheLoading() {
    assertNotNull(processCache);
    
    List<Cacheable> processList = processMapper.getAll(); //IN FACT: GIVES TIME FOR CACHE TO FINISH LOADING ASYNCH BEFORE COMPARISON BELOW...
    
    //test the cache is the same size as in DB
    assertEquals(processList.size(), processCache.getCache().getKeys().size());
    //compare all the objects from the cache and buffer
    Iterator<Cacheable> it = processList.iterator();
    while (it.hasNext()) {
      Process currentProcess = (Process) it.next();
      //equality of DataTagCacheObjects => currently only compares names
      assertEquals(currentProcess.getName(), (((Process) processCache.getCopy(currentProcess.getId())).getName()));
    }
  }
  
}
