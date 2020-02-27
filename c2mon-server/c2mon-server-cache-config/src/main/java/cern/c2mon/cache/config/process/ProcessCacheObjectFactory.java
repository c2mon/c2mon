package cern.c2mon.cache.config.process;

import cern.c2mon.cache.api.factory.AbstractCacheObjectFactory;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.PropertiesAccessor;
import cern.c2mon.shared.common.validation.MicroValidator;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.ProcessConfigurationUpdate;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou
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

    new PropertiesAccessor(properties)
      .getString("name").ifPresent(processCacheObject::setName)
      .getString("description").ifPresent(processCacheObject::setDescription)
      .getInteger("aliveInterval").ifPresent(processCacheObject::setAliveInterval)
      .getLong("aliveTagId").ifPresent(processCacheObject::setAliveTagId)
      .getLong("statusTagId").ifPresent(processCacheObject::setStateTagId)
      .getLong("stateTagId").ifPresent(processCacheObject::setStateTagId)
      .getInteger("maxMessageSize").ifPresent(processCacheObject::setMaxMessageSize)
      .getInteger("maxMessageDelay").ifPresent(processCacheObject::setMaxMessageDelay);

    return configurationUpdate;
  }

  @Override
  public void validateConfig(Process process) throws ConfigurationException {
    ProcessCacheObject processCacheObject = (ProcessCacheObject) process;

    new MicroValidator<>(processCacheObject)
      .notNull(Process::getId, "id")
      .notNull(Process::getName, "name")
      .between(processObj -> processObj.getName().length(), 1, 60)
      .not(processObj -> !ProcessCacheObject.PROCESS_NAME_PATTERN.matcher(processObj.getName()).matches(),
        "Parameter \"name\" must match the following pattern: " + ProcessCacheObject.PROCESS_NAME_PATTERN.toString())
      .notNull(Process::getDescription, "description")
      .between(processObj -> processObj.getDescription().length(), 1, 100, "description")
      .notNull(Process::getStateTagId, "stateTagId")
      .notNull(Process::getAliveTagId, "aliveTagId")
      .between(Process::getAliveInterval, 10000, Integer.MAX_VALUE)
      .between(ProcessCacheObject::getMaxMessageSize, 1, Integer.MAX_VALUE, "maxMessageSize")
      .between(ProcessCacheObject::getMaxMessageDelay, 100, Integer.MAX_VALUE, "maxMessageDelay")
      .notNull(Process::getEquipmentIds, "equipmentIds");
  }
}
