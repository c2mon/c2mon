package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheObjectFactory;
import cern.c2mon.server.cache.loading.ConfigurableDAO;
import cern.c2mon.server.configuration.handler.BaseConfigHandler;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.UnexpectedRollbackException;

import java.util.Properties;
import java.util.function.Supplier;

@Slf4j
public abstract class BaseConfigHandlerImpl<CACHEABLE extends Cacheable,RETURN> implements BaseConfigHandler<RETURN> {

  @Getter
  protected C2monCache<CACHEABLE> cache;

  protected ConfigurableDAO<CACHEABLE> cacheLoaderDAO;

  protected AbstractCacheObjectFactory<CACHEABLE> factory;

  protected Supplier<RETURN> defaultValue; // TODO (Alex) Remove this if never different

  protected BaseConfigHandlerImpl(C2monCache<CACHEABLE> cache, ConfigurableDAO<CACHEABLE> cacheLoaderDAO,
                                  AbstractCacheObjectFactory<CACHEABLE> factory, Supplier<RETURN> defaultValue) {
    this.cache = cache;
    this.cacheLoaderDAO = cacheLoaderDAO;
    this.factory = factory;
    this.defaultValue = defaultValue;
  }

  @Override
  public RETURN create(ConfigurationElement element) {
    if (cache.containsKey(element.getEntityId())) {
      throw new ConfigurationException(ConfigurationException.ENTITY_EXISTS,
        "Attempting to create a cache object with an already existing id: " + element.getEntityId());
    }

    CACHEABLE cacheObject = factory.createCacheObject(element.getEntityId(), element.getElementProperties());

    // DB insert
    cacheLoaderDAO.insert(cacheObject);

    // Cache insert
    cache.putQuiet(cacheObject.getId(), cacheObject);

    doPostCreate(cacheObject);

    return createReturnValue(cacheObject, element);
  }

  protected void doPostCreate(CACHEABLE cacheable) {

  }

  protected RETURN createReturnValue(CACHEABLE cacheable, ConfigurationElement element) {
    return defaultValue.get();
  }

  @Override
  public RETURN update(Long id, Properties properties) {
    if (!cache.containsKey(id)) {
      throw new ConfigurationException(ConfigurationException.ENTITY_DOES_NOT_EXIST,
        "Attempting to create a cache object with an already existing id: " + id);
    }

    CACHEABLE cacheObject = cache.get(id);

    Change change = factory.updateConfig(cacheObject, properties);

    cacheLoaderDAO.updateConfig(cacheObject);

    cache.putQuiet(id, cacheObject);

    doPostUpdate(cacheObject);

    // No event for DAQ layer
    return updateReturnValue(cacheObject, change, properties);
  }

  protected void doPostUpdate(CACHEABLE cacheObject) {

  }

  protected RETURN updateReturnValue(CACHEABLE cacheable, Change change, Properties properties) {
    return defaultValue.get();
  }

  protected void removeKeyIfExists(Properties properties, String key) {
    if (properties.containsKey(key)) {
      log.warn(String.format("Attempting to change the process %s  - this is not currently supported!", key));
      properties.remove(key);
    }
  }

  @Override
  public RETURN remove(Long id, ConfigurationElementReport report) {
    if (!cache.containsKey(id)) {
      log.warn("Attempting to remove a non-existent cache object - no action taken.");
      report.setWarning("Attempting to remove a non-existent cache object");
      return defaultValue.get();
    }

    try {
      CACHEABLE cacheObject = cache.get(id);

      doPreRemove(cacheObject, report);

      cacheLoaderDAO.deleteItem(id);

      cache.remove(id);

      return removeReturnValue(cacheObject, report);
    } catch (RuntimeException e) {
      log.error("Exception caught while removing a cache object.", e);
      report.setFailure("Unable to remove cache object with id " + id);
      throw new UnexpectedRollbackException("Unable to remove cache object " + id, e);
    }
  }

  protected void doPreRemove(CACHEABLE cacheable, ConfigurationElementReport report) {

  }

  protected RETURN removeReturnValue(CACHEABLE cacheable, ConfigurationElementReport report) {
    return defaultValue.get();
  }
}
