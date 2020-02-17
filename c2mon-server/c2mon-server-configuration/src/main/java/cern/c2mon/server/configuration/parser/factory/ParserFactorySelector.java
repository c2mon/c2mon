package cern.c2mon.server.configuration.parser.factory;

import cern.c2mon.server.common.util.Java9Collections;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationEntity;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;

/**
 * Collects the various {@link EntityFactory} declared and exposes a method to select the appropriate one
 * based on the passed {@link ConfigurationEntity}
 */
@Named
@Singleton
public class ParserFactorySelector {

  private HashMap<Class<?>, EntityFactory<? extends ConfigurationEntity>> classToFactoryMap;

  @Inject
  public ParserFactorySelector(
    AlarmFactory alarmFactory, CommandTagFactory commandTagFactory,
    DataTagFactory dataTagFactory, EquipmentFactory equipmentFactory, ProcessFactory processFactory, RuleTagFactory ruleTagFactory,
    SubEquipmentFactory subEquipmentFactory, AliveTagFactory aliveTagFactory, CommFaultTagFactory commFaultTagFactory,
    SupervisionStateTagFactory stateTagFactory) {
    classToFactoryMap = new HashMap<>();
    Java9Collections.listOf(alarmFactory, commandTagFactory, dataTagFactory, equipmentFactory,
      processFactory, ruleTagFactory, subEquipmentFactory, aliveTagFactory, commFaultTagFactory, stateTagFactory)
      .forEach(entityFactory -> classToFactoryMap.put(entityFactory.getEntity().getClassRef(), entityFactory));
  }

  /**
   * Determine the correct {@link EntityFactory} based on the instance of the
   * {@link ConfigurationEntity}.
   *
   * @param entity A entity for creating a {@link ConfigurationElement}.
   * @return The corresponding factory.
   */
  public EntityFactory<?> getEntityFactory(ConfigurationEntity entity) {
    if (!classToFactoryMap.containsKey(entity.getClass())) {
      throw new IllegalArgumentException("No EntityFactory for class " + entity.getClass() + " could be determined!");
    }
    return classToFactoryMap.get(entity.getClass());
  }
}
