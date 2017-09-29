package cern.c2mon.pmanager.fallback.manager;

import cern.c2mon.pmanager.IFallback;

import java.util.List;

/**
 * The number of lines read may not be equal to the
 * number of objects, if there were parse errors.
 */
public class FallbackObjectContainer {

  private final List<IFallback> objects;
  private final int readLines;

  public FallbackObjectContainer(final List<IFallback> objects, final int readLines) {
    this.objects = objects;
    this.readLines = readLines;
  }

  public List<IFallback> getObjects() {
    return this.objects;
  }

  public int getReadLines() {
    return this.readLines;
  }
}
