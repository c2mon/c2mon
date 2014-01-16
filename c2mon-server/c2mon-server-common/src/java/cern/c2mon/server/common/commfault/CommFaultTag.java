package cern.c2mon.server.common.commfault;

import cern.c2mon.shared.common.Cacheable;

public interface CommFaultTag extends Cacheable {
  
  Long getEquipmentId();

  Boolean getFaultValue();

  String getEquipmentName();

  Long getStateTagId();

  Long getAliveTagId();
  
}
