package cern.c2mon.cache.impl;

import cern.c2mon.server.common.subequipment.SubEquipment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TestIgniteC2MONCache extends IgniteC2monCache<Long, SubEquipment> {

  public TestIgniteC2MONCache() {
    super("SubEquipmentCache", null);
  }
}
