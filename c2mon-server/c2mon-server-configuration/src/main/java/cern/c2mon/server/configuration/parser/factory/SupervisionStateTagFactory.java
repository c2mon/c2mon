package cern.c2mon.server.configuration.parser.factory;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;

import static cern.c2mon.cache.config.ClientQueryProvider.queryByClientInput;

@Named
class SupervisionStateTagFactory extends EntityFactory<StatusTag> {

  private final C2monCache<cern.c2mon.server.common.supervision.SupervisionStateTag> cache;
  private SequenceDAO sequenceDAO;

  public SupervisionStateTagFactory(C2monCache<SupervisionStateTag> cache, SequenceDAO sequenceDAO) {
    super(cache);
    this.cache = cache;
    this.sequenceDAO = sequenceDAO;
  }

  @Override
  public List<ConfigurationElement> createInstance(StatusTag configurationEntity) {
    return Collections.singletonList(doCreateInstance(configurationEntity));
  }

  @Override
  Long createId(StatusTag entity) {
    if (entity.getName() != null
      && !queryByClientInput(cache, AbstractTagCacheObject::getName, entity.getName()).isEmpty()) {
      throw new ConfigurationParseException("Error creating stateTag " + entity.getName() + ": " +
        "Name already exists");
    } else {
      return entity.getId() != null ? entity.getId() : sequenceDAO.getNextTagId();
    }
  }

  @Override
  Long getId(StatusTag entity) {
    return entity != null
      ? entity.getId()
      : queryByClientInput(cache, AbstractTagCacheObject::getName, entity.getName())
      .stream().findFirst()
      .orElseThrow(CacheElementNotFoundException::new)
      .getId();
  }

  @Override
  public ConfigConstants.Entity getEntity() {
    return ConfigConstants.Entity.STATETAG;
  }
}
