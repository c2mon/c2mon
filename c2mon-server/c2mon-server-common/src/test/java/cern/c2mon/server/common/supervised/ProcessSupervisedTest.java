package cern.c2mon.server.common.supervised;

import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;

public class ProcessSupervisedTest extends AbstractSupervisedCacheObjectTest<Process> {
  @Override
  protected Process generateSample() {
    return new ProcessCacheObject();
  }
}
