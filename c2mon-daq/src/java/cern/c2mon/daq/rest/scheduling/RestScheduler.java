/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.rest.scheduling;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class holds the basic functionality for all scheduler in this Daq.
 * In general a scheduler handles all rest connections corresponding to a given tag.
 * Since everyTag is attached to a frequency this means that for each tag
 * the scheduler must provide a Thread which handles frequency.
 * Because every Scheduler handles a set of dataTag the scheduler must
 * provide the functions add, refresh and delete.
 *
 * @author Franz Ritter
 */
public abstract class RestScheduler {

  /**
   * This map saves a TimerTask for each DataTag based on the tag id.
   */
  protected Map<Long, TimerTask> idToTask;

  /**
   * The Timer class which handles the frequency scheduling.
   */
  protected Timer timer;

  /**
   * The messageSender of the given {@link EquipmentMessageHandler} which provieds the functionality to
   * send messages to the server.
   */
  protected IEquipmentMessageSender equipmentMessageSender;

  /**
   * Holds all configuration information of the given {@link EquipmentMessageHandler}.
   */
  protected IEquipmentConfiguration equipmentConfiguration;

  /**
   * Logger class of the {@link EquipmentMessageHandler} for dealing with logging.
   */
  protected EquipmentLogger equipmentLogger;

  protected RestScheduler(IEquipmentMessageSender sender, IEquipmentConfiguration configuration, EquipmentLogger logger) {
    this.equipmentMessageSender = sender;
    this.equipmentConfiguration = configuration;
    this.equipmentLogger = logger;
    this.timer = new Timer();
    this.idToTask = new HashMap<>();
  }

  /**
   *
   * Add a new DataTag to the scheduler.
   * @param id The id of the corresponding DataTag.
   */
  public abstract void addTask(Long id);

  /**
   * Sendening a refreshing request of the DataTag corresponding to the id to the scheduler.
   * @param id The id of the corresponding DataTag.
   */
  public abstract void refreshDataTag(Long id);


  /**
   * Remove a DataTag corresponding to the id from the scheduler.
   * @param id The id of the corresponding DataTag.
   */
  public void removeTask(Long id){
    if(idToTask.containsKey(id)){

      idToTask.get(id).cancel();
      idToTask.remove(id);
      timer.purge();

    } else {
      throw  new  IllegalArgumentException("Cant remove DataTag:"+id+". Tag unknown to the Equipment.");
    }
  }

  /**
   * Checks if the DataTag corresponding to the given id is known to the scheduler.
   * @param id The id of the corresponding DataTag.
   * @return
   */
  public boolean contains(Long id){
    return idToTask.containsKey(id);
  }

}
