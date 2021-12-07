package cern.c2mon.server.cache.device.query;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.device.Device;

import java.util.List;

public interface DeviceQuery {

    List<Device> findDevicesByDeviceClassId(Long deviceClassId) throws CacheElementNotFoundException;

}
