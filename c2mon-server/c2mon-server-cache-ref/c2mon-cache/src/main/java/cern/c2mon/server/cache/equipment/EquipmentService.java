package cern.c2mon.server.cache.equipment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.process.Process;

/**
 * @author Szymon Halastra
 */

@Slf4j
@Service
public class EquipmentService {

  private final C2monCache<Long, Process> processCache;

  private final C2monCache<Long, DataTag> dataTagCache;

  @Autowired
  public EquipmentService(final C2monCache<Long, Process> processCache, final C2monCache<Long, DataTag> dataTagCache) {
    this.processCache = processCache;
    this.dataTagCache = dataTagCache;
  }
}
