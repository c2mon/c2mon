package cern.c2mon.server.cache.device.query;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.device.Device;

import java.util.List;

public interface DeviceQuery {

    List<Device> findDevicesByDeviceClassId(Long deviceClassId) throws CacheElementNotFoundException;
    
    /**
     * Retrieves a particular <code>Device</code> ID from the cache by
     * specifying its name.
     *
     * @param deviceName the name of the device
     * @return the corresponding device ID, or null if no instance exists in the cache
     */
    Long findDeviceIdByName(String deviceName) throws CacheElementNotFoundException;
}
