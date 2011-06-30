/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.configuration.handler.impl;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.configuration.handler.AlarmConfigHandler;
import cern.tim.server.cache.AlarmCache;
import cern.tim.server.cache.AlarmFacade;
import cern.tim.server.cache.loading.AlarmLoaderDAO;
import cern.tim.server.common.alarm.Alarm;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;
import cern.tim.shared.common.ConfigurationException;

/**
 * See interface documentation.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class AlarmConfigHandlerImpl implements AlarmConfigHandler {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(AlarmConfigHandlerImpl.class);

  /**
   * Reference to the alarm facade.
   */
  private AlarmFacade alarmFacade;
  
  /**
   * Reference to the alarm DAO.
   */
  private AlarmLoaderDAO alarmDAO;
  
  /**
   * Reference to the alarm cache.
   */
  private AlarmCache alarmCache;
  
  /**
   * Reference to gateway to tag configuration beans.
   */
  private TagConfigGateway tagConfigGateway;
  
  /**
   * Autowired constructor.
   * @param alarmFacade the alarm facade bean
   * @param alarmDAO the alarm DAO bean
   * @param alarmCache the alarm cache bean
   * @param tagConfigGateway the tag configuration gateway bean
   */
  @Autowired
  public AlarmConfigHandlerImpl(final AlarmFacade alarmFacade, final AlarmLoaderDAO alarmDAO, 
                            final AlarmCache alarmCache, final TagConfigGateway tagConfigGateway) {
    super();
    this.alarmFacade = alarmFacade;
    this.alarmDAO = alarmDAO;
    this.alarmCache = alarmCache;
    this.tagConfigGateway = tagConfigGateway;
  }

  /**
   * Creates an alarm object in the server (puts in DB and loads into cache,
   * in that order, and updates the associated tag to point to the new
   * alarm).
   * 
   * @param element the details of the new alarm object
   * @throws IllegalAccessException should not throw the {@link IllegalAccessException} (only Tags can).
   */
  @Override
  public void createAlarm(final ConfigurationElement element) throws IllegalAccessException {
    Alarm alarm = alarmFacade.createCacheObject(element.getEntityId(), element.getElementProperties());
    alarmDAO.insert(alarm);
    alarmCache.putQuiet(alarm);

    //add alarm to tag
    tagConfigGateway.addAlarmToTag(alarm.getTagId(), alarm.getId());
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
  @Transactional("cacheTransactionManager")
  public void updateAlarm(final Long alarmId, final Properties properties) {
    //reject if trying to change datatag it is attached to - not currently allowed
    if (properties.containsKey("dataTagId")) {
      throw new ConfigurationException(ConfigurationException.UNDEFINED, 
          "Attempting to change the tag to which the alarm is attached - this is not currently supported!");
    }
    Alarm alarm = alarmCache.get(alarmId);    
    try {
      alarm.getWriteLock().lock();
      alarmFacade.updateConfig(alarm, properties);
      alarmDAO.updateConfig(alarm);
    } catch (Exception ex) {      
      LOGGER.error("Exception caught while updating alarm" + alarmId, ex);
      throw new ConfigurationException(ConfigurationException.UNDEFINED, ex);
    } finally {
      alarm.getWriteLock().unlock();
    }            
  }

  /**
   * Removes the alarm from the system (including datatag reference to it).
   * 
   * <p>In more detail, removes the reference to the alarm in the associated
   * tag, removes the alarm from the DB and removes the alarm form the cache,
   * in that order.
   * 
   * <p>Ready to be called when removing a datatag (not currently done as require
   * manual removal of alarms first).
   * 
   * @param alarmId the id of the alarm to remove
   * @param alarmReport the configuration report for the alarm removal
   */
  @Override
  @Transactional("cacheTransactionManager")
  public void removeAlarm(final Long alarmId, final ConfigurationElementReport alarmReport) {
    Alarm alarm = alarmCache.get(alarmId);    
    try {
      alarm.getWriteLock().lock();
      removeDataTagReference(alarm);
      alarmDAO.deleteItem(alarmId);
      alarmCache.remove(alarmId);
    } catch (Exception ex) {      
      LOGGER.error("Exception caught while removing Alarm " + alarmId, ex);
      alarmReport.setFailure("Unable to remove Alarm with id " + alarmId);
      throw new ConfigurationException(ConfigurationException.UNDEFINED, ex);
    } finally {
      alarm.getWriteLock().unlock();
    }
  }

  /**
   * Removes the reference to the alarm in the associated Tag object.
   * @param alarm the alarm for which the tag needs updating
   */
  private void removeDataTagReference(final Alarm alarm) { 
    tagConfigGateway.removeAlarmFromTag(alarm.getTagId(), alarm.getId());
  }

}
