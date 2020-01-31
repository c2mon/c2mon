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

import cern.c2mon.cache.actions.alarm.AlarmCacheObjectFactory;
import cern.c2mon.cache.actions.alarm.AlarmService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.collections.TagCacheCollection;
import cern.c2mon.server.cache.loading.AlarmLoaderDAO;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.listener.ConfigurationEventListener;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.GenericApplicationContext;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Bean managing configuration updates to C2MON Alarms.
 *
 * @author Alexandros Papageorgiou
 */
@Named
@Slf4j
public class AlarmConfigHandler extends BaseConfigHandlerImpl<Alarm> {

  private final Collection<ConfigurationEventListener> configurationEventListeners;

  private final TagCacheCollection unifiedTagCacheFacade;
  private AlarmService alarmService;

  @Inject
  public AlarmConfigHandler(final C2monCache<Alarm> alarmCache, final AlarmLoaderDAO alarmDAO,
                            final AlarmCacheObjectFactory alarmCacheObjectFactory,
                            final GenericApplicationContext context,
                            final TagCacheCollection unifiedTagCacheFacade,
                            final AlarmService alarmService) {
    super(alarmCache, alarmDAO, alarmCacheObjectFactory, ArrayList::new);
    this.configurationEventListeners = context.getBeansOfType(ConfigurationEventListener.class).values();
    this.unifiedTagCacheFacade = unifiedTagCacheFacade;
    this.alarmService = alarmService;
  }

  @Override
  protected void doPostCreate(Alarm alarm) {
    for (ConfigurationEventListener listener : this.configurationEventListeners) {
      listener.onConfigurationEvent(alarm, ConfigConstants.Action.CREATE);
    }

    unifiedTagCacheFacade.addAlarmToTag(alarm.getDataTagId(), alarm.getId());

    alarmService.evaluateAlarm(alarm.getId());
  }

  /**
   * Updates the Alarm object in the server from the provided Properties.
   * In more detail, updates the cache, then the DB.
   *
   * <p>Note that moving the alarm to a different tag is not allowed. In
   * this case the alarm should be removed and recreated.
   * @param alarmId the id of the alarm
   * @param properties the update details
   */
  @Override
  public List<ProcessChange> update(final Long alarmId, final Properties properties) {
    removeKeyIfExists(properties, "dataTagId");

    super.update(alarmId, properties);

    alarmService.evaluateAlarm(alarmId);

    return defaultValue.get();
  }

  @Override
  protected void doPreRemove(Alarm alarm, ConfigurationElementReport report) {
    for (ConfigurationEventListener listener : this.configurationEventListeners) {
      listener.onConfigurationEvent(alarm, ConfigConstants.Action.REMOVE);
    }

    unifiedTagCacheFacade.removeAlarmFromTag(alarm.getDataTagId(), alarm.getId());
  }
}
