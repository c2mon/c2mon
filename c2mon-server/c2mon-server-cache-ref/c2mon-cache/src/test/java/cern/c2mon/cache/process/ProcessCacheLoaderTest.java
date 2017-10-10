package cern.c2mon.cache.process;

import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.Cache;
import cern.c2mon.server.cache.dbaccess.ProcessMapper;
import cern.c2mon.server.common.datatag.DataTag;
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
  private Cache<Long, Process> processCacheRef;

  @Before
  public void init() {
    processCacheRef.init();
  }

  @Test
  public void preloadCache() {
    assertNotNull("Process Cache should not be null", processCacheRef);

    List<DataTag> processList = processMapper.getAll();

    assertTrue("List of process tags should not be empty", processList.size() > 0);

    assertEquals("Size of cache and DB mapping should be equal", processList.size(), processCacheRef.getKeys().size());
    //compare all the objects from the cache and buffer
    Iterator<DataTag> it = processList.iterator();
    while (it.hasNext()) {
      Process currentProcess = (Process) it.next();
      //equality of DataTagCacheObjects => currently only compares names
      assertEquals("Cached Process should have the same name as in DB",
              currentProcess.getName(), ((processCacheRef.get(currentProcess.getId())).getName()));
    }
  }
}
