package cern.c2mon.cache.process;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import cern.c2mon.server.cache.dbaccess.ProcessMapper;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * This is an integration test for loading cache from DB using embedded cache
 *
 * @author Szymon Halastra
 */
public class ProcessCacheLoaderTest extends AbstractCacheLoaderTest<Process> {

  @Autowired
  private ProcessMapper processMapper;

  @Autowired
  private C2monCache<Process> processCacheRef;

  @Override
  protected LoaderMapper<Process> getMapper() {
    return processMapper;
  }

  @Override
  protected void customCompare(List<Process> mapperList, Map<Long, Process> cacheList) throws ClassNotFoundException {
    for (Process process : mapperList) {
      Process currentProcess = (Process) process;
      //equality of DataTagCacheObjects => currently only compares names
      assertEquals("Cached Process should have the same name as in DB",
        currentProcess.getName(), ((cacheList.get(currentProcess.getId())).getName()));
    }
  }

  @Override
  protected Process getSample() {
    return new ProcessCacheObject(0L);
  }

  @Override
  protected Long getExistingKey() {
    return 50L;
  }

  @Override
  protected C2monCache<Process> getCache() {
    return processCacheRef;
  }
}
