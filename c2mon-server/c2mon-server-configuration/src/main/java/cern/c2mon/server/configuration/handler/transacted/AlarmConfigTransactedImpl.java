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
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.AlarmLoaderDAO;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.listener.ConfigurationEventListener;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;

import java.util.Collection;
import java.util.Properties;

/**
 * Bean managing configuration updates to C2MON Alarms.
 *
 * @author Alexandros Papageorgiou
 */
@Slf4j
@Service
public class AlarmConfigTransactedImpl extends BaseConfigHandlerImpl<Alarm, Void> {

  private final Collection<ConfigurationEventListener> configurationEventListeners;

  private AlarmService alarmService;

  @Autowired
  public AlarmConfigTransactedImpl(final C2monCache<Alarm> alarmCache, final AlarmLoaderDAO alarmDAO,
                                   final AlarmCacheObjectFactory alarmCacheObjectFactory,
                                   final GenericApplicationContext context,
                                   final AlarmService alarmService) {
    super(alarmCache, alarmDAO, alarmCacheObjectFactory, __ -> null, __ -> null);
    this.configurationEventListeners = context.getBeansOfType(ConfigurationEventListener.class).values();
    this.alarmService = alarmService;
  }

  @Override
  protected void doPostCreate(Alarm alarm) {
    for (ConfigurationEventListener listener : this.configurationEventListeners) {
      listener.onConfigurationEvent(alarm, ConfigConstants.Action.CREATE);
    }

    // TODO (Alex) This is horrible, remove it
    try {
      tagConfigGateway.addAlarmToTag(alarm.getDataTagId(), alarm.getId());
    } catch (Exception e) {
      log.error("Exception caught while adding new Alarm " + alarm.getId() + " to Tag " + alarm.getId(), e);
      cache.remove(alarm.getId());
      tagConfigGateway.removeAlarmFromTag(alarm.getDataTagId(), alarm.getId());
      throw new UnexpectedRollbackException("Unexpected exception while creating a Alarm " + alarm.getId() + ": rolling back the creation", e);
    }

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
  public Void update(final Long alarmId, final Properties properties) {
    removeKeyIfExists(properties, "dataTagId");

    super.update(alarmId, properties);

    alarmService.evaluateAlarm(alarmId);

    return null;
  }

  @Override
  protected void doPreRemove(Alarm alarm, ConfigurationElementReport report) {
    for (ConfigurationEventListener listener : this.configurationEventListeners) {
      listener.onConfigurationEvent(alarm, ConfigConstants.Action.REMOVE);
    }

    try {
      removeDataTagReference(alarm);
    } catch (CacheElementNotFoundException e) {
      log.warn("Unable to remove Alarm reference from Tag, as could not locate Tag " + alarm.getDataTagId() + " in cache");
    }
  }

  /**
   * Removes the reference to the alarm in the associated Tag object.
   * @param alarm the alarm for which the tag needs updating
   */
  private void removeDataTagReference(final Alarm alarm) {
    tagConfigGateway.removeAlarmFromTag(alarm.getDataTagId(), alarm.getId());
  }

}
