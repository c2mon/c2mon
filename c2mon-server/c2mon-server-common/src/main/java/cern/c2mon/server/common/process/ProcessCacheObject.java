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
package cern.c2mon.server.common.process;

import cern.c2mon.server.common.equipment.AbstractSupervisedCacheObject;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Cache object representing the C2MON DAQ processes. This object usually
 * resides in the cache when accessed, in which case care must be taken with
 * synchronization:
 * <p>
 * Methods exposed by the Process interface are thread safe and can safely be
 * used to access details about the process, whether in or out the cache. Setter
 * methods are usually not exposed, and modifications to the process objects in
 * the cache should preferably be made using the ProcessFacade bean. Setter
 * methods not exposed in the Process interface are in general NOT thread safe
 * and should therefore only be used on clones of the Process object residing
 * outside the cache.
 */
@Slf4j
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProcessCacheObject extends AbstractSupervisedCacheObject implements Process {

  public static final Pattern PROCESS_NAME_PATTERN = Pattern.compile("P_([A-Z_0-9])+", Pattern.CASE_INSENSITIVE);
  public static final String LOCAL_CONFIG = "Y";
  public static final String SERVER_CONFIG = "N";
  private static final long serialVersionUID = -2235204911515127976L;

  private final SupervisionEntity supervisionEntity = SupervisionEntity.PROCESS;
  /**
   * A description of the process.
   * -- GETTER --
   * Get an optional free-text description for the process.
   */
  private String description;

  /**
   * Max number of updates in a single message from the DAQ process.
   * -- GETTER --
   * Get the maximum number of value updates to be sent by the DAQ at once. If
   * the DAQ wants to send more value updates than maxMessageSize (e.g. on
   * start-up), the updates have to be split into several JMS messages.
   *
   * @return max number of updates as int
   */
  private int maxMessageSize = 100;

  /**
   * Max delay between reception of update by a DAQ and sending it to the
   * server.
   * -- GETTER --
   * Get the maximum number of milliseconds a value update message may be
   * delayed by the DAQ in order to bundle several value updates together. If
   * only one value update is to be sent by the DAQ, the DAQ may wait up to
   * maxMessageDelay milliseconds before sending the message to the application
   * server.
   *
   * @return max delay in milliseconds
   */
  private int maxMessageDelay = 1000;

  /**
   * Equipments under this Process.
   */
  private ArrayList<Long> equipmentIds = new ArrayList<>();

  /**
   * Host the DAQ process is running on.
   * -- GETTER --
   * If the DAQ process is currently (believed to be) running, this method will
   * return the name of the host on which the DAQ process has been started. If
   * the DAQ process is believed to be DOWN, the method will return null.
   *
   * @return the host
   */
  private String currentHost;

  /**
   * Time the DAQ process was started up.
   * -- GETTER --
   * If the DAQ process is currently (known to be) running, this method will
   * return the start-up time of the DAQ process. If the DAQ process is believed
   * to be DOWN, this method will return null.
   */
  private Timestamp startupTime;

  /**
   * Indicates the DAQ needs rebooting to obtain the latest configuration from
   * the server.
   */
  private Boolean requiresReboot = Boolean.FALSE;

  /**
   * Process Identifier Key (PIK) per DAQ instance
   */
  private Long processPIK;
  /**
   * The configuration type can be whether Y (LOCAL_CONFIG) or N (SERVER_CONFIG)
   */
  private LocalConfig localConfig;

  /**
   * Constructor with minimal number of non-null fields. Used when loading from
   * the DB at start up.
   *
   * @param id              the id of the process
   * @param name            the name of the process
   * @param stateTagId      the id of the state tag
   * @param maxMessageSize  the max number of updates per message from DAQ layer
   * @param maxMessageDelay the max delay at DAQ layer before sending the
   *                        updates
   */
  public ProcessCacheObject(Long id, String name, Long stateTagId, Integer maxMessageSize, Integer maxMessageDelay) {
    super(id, stateTagId);
    setName(name);
    this.maxMessageSize = maxMessageSize;
    this.maxMessageDelay = maxMessageDelay;
  }

  /**
   * Constructor only setting id.
   *
   * @param id process id
   */
  public ProcessCacheObject(final Long id) {
    super(id);
  }

  @SuppressWarnings("unchecked")
  @Override
  public ProcessCacheObject clone() throws CloneNotSupportedException {
    ProcessCacheObject clone = (ProcessCacheObject) super.clone();
    clone.equipmentIds = (ArrayList<Long>) equipmentIds.clone();
    if (this.startupTime != null) {
      clone.startupTime = (Timestamp) this.startupTime.clone();
    }

    return clone;
  }

  @Override
  public void stop(Timestamp timestamp) {
    super.stop(timestamp);
    setCurrentHost(null);
    setStartupTime(null);
    setRequiresReboot(Boolean.FALSE);
    setProcessPIK(null);
    setLocalConfig(null);
  }

  /**
   * Enum for describing configuration type
   * <p>
   * Y for LOCAL_CONFIG or N for SERVER_CONFIG
   */
  public enum LocalConfig {
    Y("LOCAL_CONFIG"), N("SERVER_CONFIG");

    private String configType;

    LocalConfig(String configType) {
      this.configType = configType;
    }

    public String getConfigType() {
      return this.configType;
    }
  }
}
