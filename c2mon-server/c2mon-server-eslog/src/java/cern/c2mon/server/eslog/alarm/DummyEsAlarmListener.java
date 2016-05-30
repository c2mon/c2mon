package cern.c2mon.server.eslog.alarm;
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

import cern.c2mon.pmanager.IAlarmListener;

import java.io.File;

/**
 * Not implemented yet; TODO once alarm module is defined.
 *
 * @author Mark Brightwell
 */
public class DummyEsAlarmListener implements IAlarmListener {

  @Override
  public void dbUnavailable(boolean alarmUp, String exceptionMsg, String dbInfo) {
    // TODO Auto-generated method stub

  }

  @Override
  public void diskFull(boolean alarmUp, String directoryName) {
    // TODO Auto-generated method stub

  }

  @Override
  public void fileNotReachable(boolean alarmUp, File file) {
    // TODO Auto-generated method stub

  }
}