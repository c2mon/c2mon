package cern.c2mon.server.cache.process;

import cern.c2mon.cache.api.factory.AbstractCacheObjectFactory;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.ProcessConfigurationUpdate;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * @author Szymon Halastra
 */
@Component
public class ProcessCacheObjectFactory extends AbstractCacheObjectFactory<Process> {

  @Override
  public Process createCacheObject(Long id) {
    return new ProcessCacheObject(id);
  }

  @Override
  public Change configureCacheObject(Process process, Properties properties) {
    ProcessCacheObject processCacheObject = (ProcessCacheObject) process;
    ProcessConfigurationUpdate configurationUpdate = new ProcessConfigurationUpdate();
    configurationUpdate.setProcessId(process.getId());
    String tmpStr = null;
    if (properties.getProperty("name") != null) {
      processCacheObject.setName(properties.getProperty("name"));
    }
    if (properties.getProperty("description") != null) {
      processCacheObject.setDescription(properties.getProperty("description"));
    }
    if ((tmpStr = properties.getProperty("aliveInterval")) != null) {
      try {
        processCacheObject.setAliveInterval(Integer.valueOf(tmpStr));
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"aliveInterval\" to Integer: " + tmpStr);
      }
    }
    if ((tmpStr = properties.getProperty("aliveTagId")) != null) {
      try {
        processCacheObject.setAliveTagId(Long.valueOf(tmpStr));
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"aliveTagId\" to Long: " + tmpStr);
      }
    }
    if ((tmpStr = properties.getProperty("stateTagId")) != null || (tmpStr = properties.getProperty("statusTagId")) != null) {
      try {
        processCacheObject.setStateTagId(Long.valueOf(tmpStr));
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"stateTagId\" to Long: " + tmpStr);
      }
    }
    if ((tmpStr = properties.getProperty("maxMessageSize")) != null) {
      processCacheObject.setMaxMessageSize(parseInt(tmpStr, "maxMessageSize"));
    }

    if ((tmpStr = properties.getProperty("maxMessageDelay")) != null) {
      processCacheObject.setMaxMessageDelay(parseInt(tmpStr, "maxMessageDelay"));
    }

    return configurationUpdate;
  }

  @Override
  public void validateConfig(Process process) throws ConfigurationException {
    ProcessCacheObject processCacheObject = (ProcessCacheObject) process;
    if (processCacheObject.getId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"id\" cannot be null");
    }
    if (processCacheObject.getName() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be null");
    }
    if (processCacheObject.getName().length() == 0 || processCacheObject.getName().length() > 60) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" must be 1 to 60 characters long");
    }
    if (!ProcessCacheObject.PROCESS_NAME_PATTERN.matcher(processCacheObject.getName()).matches()) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" must match the following pattern: " + ProcessCacheObject.PROCESS_NAME_PATTERN.toString());
    }
    if (processCacheObject.getDescription() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"description\" cannot be null");
    }
    if (processCacheObject.getDescription().length() == 0 || processCacheObject.getDescription().length() > 100) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"description\" must be 1 to 100 characters long");
    }
    if (processCacheObject.getStateTagId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"stateTagId\" cannot be null");
    }
    if (processCacheObject.getAliveTagId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"aliveTagId\" cannot be null");
    }
    if (processCacheObject.getAliveInterval() < 10000) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"aliveInterval\" must be >= 10000 milliseconds");
    }
    if (processCacheObject.getMaxMessageSize() < 1) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"maxMessageSize\" must be >= 1");
    }
    if (processCacheObject.getMaxMessageDelay() < 100) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"maxMessageDelay\" must be >= 100");
    }
    if (processCacheObject.getEquipmentIds() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Collection \"equipmentIds\" cannot be null");
    }
  }
}
