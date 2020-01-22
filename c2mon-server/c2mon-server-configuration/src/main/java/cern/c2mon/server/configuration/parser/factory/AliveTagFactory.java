package cern.c2mon.server.configuration.parser.factory;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.tag.AliveTag;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;

import static cern.c2mon.cache.config.ClientQueryProvider.queryByClientInput;

@Named
public class AliveTagFactory extends EntityFactory<AliveTag> {

  private final C2monCache<cern.c2mon.server.common.alive.AliveTag> cache;
  private SequenceDAO sequenceDAO;

  @Inject
  public AliveTagFactory(C2monCache<cern.c2mon.server.common.alive.AliveTag> aliveTagCache, SequenceDAO sequenceDAO) {
    super(aliveTagCache);
    cache = aliveTagCache;
    this.sequenceDAO = sequenceDAO;
  }

  @Override
  public List<ConfigurationElement> createInstance(AliveTag configurationEntity) {
    return Collections.singletonList(doCreateInstance(configurationEntity));
  }

  @Override
  Long createId(AliveTag configurationEntity) {

    if (configurationEntity.getName() != null
      && !queryByClientInput(cache, aTag -> aTag.getSupervisedName(), configurationEntity.getName()).isEmpty()) {
      throw new ConfigurationParseException("Error creating aliveTag " + configurationEntity.getName() + ": " +
        "Name already exists");
    } else {
      return configurationEntity.getId() != null ? configurationEntity.getId() : sequenceDAO.getNextTagId();
    }
  }

  @Override
  Long getId(AliveTag configurationEntity) {
    return configurationEntity.getId() != null
      ? configurationEntity.getId()
      : queryByClientInput(cache, aTag -> aTag.getSupervisedName(), configurationEntity.getName())
        .stream().findFirst()
        .orElseThrow(CacheElementNotFoundException::new)
        .getId();
  }

  @Override
  boolean hasEntity(Long id) {
    return id != null && cache.containsKey(id);
  }

  @Override
  ConfigConstants.Entity getEntity() {
    return ConfigConstants.Entity.CONTROLTAG;
  }
}
