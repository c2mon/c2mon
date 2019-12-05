/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.elasticsearch.fallback;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cern.c2mon.pmanager.IAlarmListener;

/**
 * Listens for fallback activation/deactivation events and sends warning emails
 * and SMS messages.
 *
 * @author Alban Marguet
 */
@Component
public class FallbackActivationListener implements IAlarmListener {

  private static final Logger mail = LoggerFactory.getLogger("AdminMailLogger");
  private static final Logger sms = LoggerFactory.getLogger("AdminSmsLogger");

  /**
   * Flags for not sending repeated error messages.
   */
  private volatile boolean dbAlarm = false;
  private volatile boolean diskAlarm = false;
  private volatile boolean fileAlarm = false;

  @Override
  public void dbUnavailable(boolean alarmUp, String exceptionMsg, String dbInfo) {
    if (alarmUp && !dbAlarm) {
      dbAlarm = true;
      mail.error("Error logging to Elasticsearch ({}): {}", dbInfo, exceptionMsg);
      sms.error("Error logging to Elasticsearch ({}): {}", dbInfo, exceptionMsg);
    } else if (!alarmUp && dbAlarm) {
      dbAlarm = false;
      mail.error("Elasticsearch error has resolved itself");
      sms.error("Elasticsearch error has resolved itself");
    }
  }

  @Override
  public void diskFull(boolean alarmUp, String directoryName) {
    if (alarmUp && !diskAlarm) {
      diskAlarm = true;
      mail.error("Error in Elasticsearch fallback: disk is nearly full: directory is {}", directoryName);
      sms.error("Error in Elasticsearch fallback: disk is nearly full: directory is {}", directoryName);
    } else if (!alarmUp && diskAlarm) {
      diskAlarm = false;
      mail.error("Disk full error has resolved itself");
      sms.error("Disk full error has resolved itself");
    }
  }

  @Override
  public void fileNotReachable(boolean alarmUp, File file) {
    if (alarmUp && !fileAlarm) {
      fileAlarm = true;
      mail.error("Error in Elasticsearch fallback: file not reachable: {}", file.getName());
      sms.error("Error in Elasticsearch fallback: file not reachable: {}", file.getName());
    } else if (!alarmUp && fileAlarm) {
      fileAlarm = false;
      mail.error("File unreachable error has resolved itself");
      sms.error("File unreachable error has resolved itself");
    }
  }
}
