package cern.c2mon.server.cache.device.query;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;

public interface DeviceClassQuery {

    Long findDeviceByName(String deviceClassName) throws CacheElementNotFoundException;

}
