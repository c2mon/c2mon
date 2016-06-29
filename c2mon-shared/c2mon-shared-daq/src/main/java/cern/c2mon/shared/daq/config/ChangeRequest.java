/*******************************************************************************
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
 ******************************************************************************/

package cern.c2mon.shared.daq.config;

import cern.c2mon.shared.daq.messaging.ServerRequest;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Franz Ritter
 */
@Data
public class ChangeRequest implements ServerRequest {

  List<Change> changeList = new ArrayList<>();

  public ChangeRequest() {
  }

  public ChangeRequest(final List<? extends Change> changes) {
    this.changeList = (List<Change>) changes;
  }

  public <T extends Change> void addChange(T item) {
    changeList.add(item);
  }

  @SuppressWarnings("unchecked")
  public <T extends Change> void setChangeList(List<T> entities) {
    this.changeList = (List<Change>) entities;
  }

}
