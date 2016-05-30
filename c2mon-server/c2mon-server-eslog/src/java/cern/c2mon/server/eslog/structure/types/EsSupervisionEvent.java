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
package cern.c2mon.server.eslog.structure.types;

import cern.c2mon.pmanager.IFallback;
import com.google.gson.Gson;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents a SupervisionEvent for ElasticSearch.
 *
 * @author Alban Marguet
 */
@Slf4j
@Data
public class EsSupervisionEvent implements IFallback {
  @NonNull
  private final transient Gson gson = GsonSupplier.INSTANCE.get();

  private long entityId;
  private String message;
  private String entityName;
  private String statusName;
  private long eventTime;

  /**
   * JSON representation of the EsSupervisionEvent
   */
  @Override
  public String toString() {
    return gson.toJson(this);
  }

  @Override
  public IFallback getObject(String line) {
    return gson.fromJson(line, EsSupervisionEvent.class);
  }

  @Override
  public String getId() {
    return String.valueOf(entityId);
  }
}