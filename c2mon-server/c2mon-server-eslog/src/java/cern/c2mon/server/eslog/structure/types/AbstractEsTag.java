/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.eslog.structure.types;

import cern.c2mon.pmanager.IFallback;
import com.google.gson.Gson;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that represents a Tag for ElasticSearch.
 * Used as "type" in ElasticSearch.
 *
 * @author Alban Marguet.
 */
@Slf4j
@Data
public abstract class AbstractEsTag implements IFallback {

  @NonNull
  protected final transient Gson gson = GsonSupplier.INSTANCE.get();

  private long id;
  private String name;
  private String dataType;
  private long sourceTimestamp;
  private long serverTimestamp;
  private long daqTimestamp;
  private int status;
  private String quality; //tagstatusdesc
  private Boolean valid; //if quality is OK or not

  protected transient Object value;
  protected Boolean valueBoolean;
  protected String valueString;
  protected Number valueNumeric;
  private String valueDescription;

  private final Map<String, String> metadata = new HashMap<>();

  private String process;
  private String equipment;
  private String subEquipment;

  abstract public void setValue(Object tagValue);

  /**
   * JSON representation of a AbstractEsTag.
   */
  @Override
  public String toString() {
    String json = gson.toJson(this);
    log.debug(json);
    return json;
  }

  @Override
  public abstract IFallback getObject(String line);

  @Override
  public String getId() {
    return String.valueOf(id);
  }

  public long getIdAsLong() {
    return id;
  }

}