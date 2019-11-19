package cern.c2mon.server.supervision.impl.event;

import cern.c2mon.server.common.process.Process;

public class ProcessEvents implements SupervisionEventHandler<Process> {

  @Override
  public void onUp(Process supervised) {

  }

  @Override
  public void onDown(Process supervised) {

  }
}
