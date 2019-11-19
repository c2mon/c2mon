package cern.c2mon.cache.actions.device;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.flow.DefaultC2monCacheFlow;
import cern.c2mon.server.common.device.Device;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou
 */
@Service
public class DeviceService extends AbstractCacheServiceImpl<Device> {

  @Inject
  public DeviceService(final C2monCache<Device> deviceCacheRef) {
    super(deviceCacheRef, new DefaultC2monCacheFlow<>());
  }
}
