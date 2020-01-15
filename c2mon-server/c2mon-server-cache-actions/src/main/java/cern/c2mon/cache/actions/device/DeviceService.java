package cern.c2mon.cache.actions.device;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.actions.deviceclass.DeviceClassService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.shared.client.device.DeviceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

import static cern.c2mon.server.common.util.Java9Collections.setOf;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou
 */
@Slf4j
@Service
public class DeviceService extends AbstractCacheServiceImpl<Device> {

  private final DeviceClassService deviceClassService;

  @Inject
  public DeviceService(final C2monCache<Device> deviceCacheRef, DeviceClassService deviceClassService) {
    super(deviceCacheRef, new DefaultCacheFlow<>());
    this.deviceClassService = deviceClassService;
  }

  public Collection<Device> getDevices(String deviceClassName) {
    Collection<Device> devices = new ArrayList<>();

    try {
      // Search the name attribute of the class cache
      long deviceClassId = deviceClassService.getIdByName(deviceClassName);

      devices = getByDeviceClassId(deviceClassId);

    } catch (CacheElementNotFoundException e) {
      // If we didn't find a class with the given name, return an empty list.
      log.warn("Error getting device class by name", e);
      return devices;
    }

    return devices;
  }

  private Collection<Device> getByDeviceClassId(long deviceClassId) {
    return cache.query(device -> device.getDeviceClassId() == deviceClassId);
  }

  public List<Device> getDevices(Set<DeviceInfo> deviceInfoList) {
    List<Device> devices = new ArrayList<>();

    // Reorganise the data structure to make processing a bit easier
    Map<String, Set<String>> classNamesToDeviceNames = new HashMap<>();
    for (DeviceInfo deviceInfo : deviceInfoList) {
      if (!classNamesToDeviceNames.containsKey(deviceInfo.getClassName())) {
        classNamesToDeviceNames.put(deviceInfo.getClassName(), setOf(deviceInfo.getDeviceName()));
      } else {
        classNamesToDeviceNames.get(deviceInfo.getClassName()).add(deviceInfo.getDeviceName());
      }
    }

    // Build up a list of requested devices. Note that this list may not be
    // complete. It is the client's responsibility to check the completeness of
    // the returned list.
    for (Map.Entry<String, Set<String>> entry : classNamesToDeviceNames.entrySet()) {
      String className = entry.getKey();
      Set<String> deviceNames = entry.getValue();

      try {
        Collection<Device> deviceList = getDevices(className);

        for (Device device : deviceList) {
          if (deviceNames.contains(device.getName())) {
            devices.add(device);
          }
        }

      } catch (CacheElementNotFoundException e) {
        log.warn("Didn't find any devices of class " + className, e);
      }
    }

    return devices;
  }
}
