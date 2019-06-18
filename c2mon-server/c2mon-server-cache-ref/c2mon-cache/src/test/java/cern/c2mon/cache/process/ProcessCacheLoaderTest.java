package cern.c2mon.cache.process;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.ProcessMapper;
import cern.c2mon.server.common.process.Process;

import static org.junit.Assert.*;

/**
 * This is an integration test for loading cache from DB using embedded cache
 *
 * @author Szymon Halastra
 */
public class ProcessCacheLoaderTest extends AbstractCacheLoaderTest {

  @Autowired
  private ProcessMapper processMapper;

  @Autowired
  private C2monCache<Long, Process> processCacheRef;

  @Before
  public void init() {
    processCacheRef.init();
  }

  @Test
  @Ignore
  public void preloadCache() {
    assertNotNull("Process Cache should not be null", processCacheRef);

    List<Process> processList = processMapper.getAll();

    assertTrue("List of process tags should not be empty", processList.size() > 0);

    assertEquals("Size of cache and DB mapping should be equal", processList.size(), processCacheRef.getKeys().size());
    //compare all the objects from the cache and buffer
    for (Process process : processList) {
      Process currentProcess = (Process) process;
      //equality of DataTagCacheObjects => currently only compares names
      assertEquals("Cached Process should have the same name as in DB",
              currentProcess.getName(), ((processCacheRef.get(currentProcess.getId())).getName()));
    }
  }
}
