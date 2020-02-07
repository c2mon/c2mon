/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheObjectFactory;
import cern.c2mon.cache.config.collections.TagCacheCollection;
import cern.c2mon.server.cache.loading.ConfigurableDAO;
import cern.c2mon.server.common.listener.ConfigurationEventListener;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.GenericApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Public methods in this class should perform the complete
 * configuration process for the given tag (i.e. cache update
 * and database persistence).
 * <p>
 * The methods contain the common reconfiguration logic for
 * all Tag objects (Control, Data and Rule tags).
 * <p>
 * Notice that these methods will always be called within
 * a transaction initiated at the ConfigurationLoader level
 * and passed through the handler via a "create", "update"
 * or "remove" method, with rollback of DB changes if a
 * RuntimeException is thrown.
 *
 * @param <TAG> the type of Tag
 * @author Alexandros Papageorgiou, Mark Brightwell
 */
@Slf4j
abstract class AbstractTagConfigHandler<TAG extends Tag> extends BaseConfigHandlerImpl<TAG> {

  protected final TagCacheCollection tagCacheCollection;
  protected final Collection<ConfigurationEventListener> configurationEventListeners;
  protected final AlarmConfigHandler alarmConfigHandler;

  public AbstractTagConfigHandler(final C2monCache<TAG> tagCache,
                                  final ConfigurableDAO<TAG> configurableDAO,
                                  final AbstractCacheObjectFactory<TAG> tagCacheObjectFactory,
                                  final TagCacheCollection tagCacheCollection,
                                  final GenericApplicationContext context,
                                  final AlarmConfigHandler alarmConfigHandler) {
    super(tagCache, configurableDAO, tagCacheObjectFactory, ArrayList::new);
    this.tagCacheCollection = tagCacheCollection;
    this.configurationEventListeners = context.getBeansOfType(ConfigurationEventListener.class).values();
    this.alarmConfigHandler = alarmConfigHandler;
  }

  @Override
  protected void doPostCreate(TAG cacheable) {
    super.doPostCreate(cacheable);
    for (ConfigurationEventListener listener : configurationEventListeners) {
      listener.onConfigurationEvent(cacheable, ConfigConstants.Action.CREATE);
    }
  }

  @Override
  protected void doPostUpdate(TAG cacheable) {
    super.doPostUpdate(cacheable);
    for (ConfigurationEventListener listener : configurationEventListeners) {
      listener.onConfigurationEvent(cacheable, ConfigConstants.Action.UPDATE);
    }
  }

  /**
   * Cascades removal into the alarms connected with this tag
   *
   * @param tag    the Tag for removal
   * @param report the report on this action
   */
  @Override
  protected void doPreRemove(TAG tag, ConfigurationElementReport report) {
    super.doPreRemove(tag, report);

    tag.getAlarmIds().forEach(alarmId -> {
      report.addSubReport(
        new ConfigurationElementReport(ConfigConstants.Action.REMOVE, ConfigConstants.Entity.ALARM, alarmId));
      alarmConfigHandler.remove(alarmId, report);
    });
  }

  Collection<ConfigurationElementReport> createConfigRemovalReportsFor(ConfigConstants.Entity entity, Collection<Long> ids, C2monCache<?> targetCache) {
    return ids.stream()
      .filter(targetCache::containsKey)
      .map(id -> new ConfigurationElementReport(ConfigConstants.Action.REMOVE, entity, id))
      .collect(Collectors.toList());
  }
}

