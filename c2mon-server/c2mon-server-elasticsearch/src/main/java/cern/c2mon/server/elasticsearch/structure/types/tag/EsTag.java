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
package cern.c2mon.server.elasticsearch.structure.types.tag;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import cern.c2mon.pmanager.IFallback;
import cern.c2mon.server.elasticsearch.structure.types.GsonSupplier;
import cern.c2mon.shared.common.type.TypeConverter;

/**
 * Class that represents a Tag for Elasticsearch.
 * Used as "type" in Elasticsearch.
 *
 * @author Alban Marguet.
 */
@Slf4j
@Data
public class EsTag implements IFallback {

  @NonNull
  protected static final transient Gson gson = GsonSupplier.INSTANCE.get();

  public static final transient String TYPE_STRING  = "string";
  public static final transient String TYPE_NUMBER  = "number";
  public static final transient String TYPE_BOOLEAN = "boolean";
  public static final transient String TYPE_OBJECT  = "object";


  private final long id;
  private String name;

  protected Number value;
  protected Long valueLong;
  protected Boolean valueBoolean;
  protected String valueString;

  private String valueDescription;

  /**
   * The nullable metric unit of the enclosed tag value.
   */
  private String unit;

  /** either "string", "number", "boolean" or "object" */
  @Setter(AccessLevel.NONE)
  private String type;

  /**
   * Contains useful information about the validity, status and invalid
   * qualities for all the monitored instances (process, equipment, sub-equipment),
   * that are registered under this tag.
   */
  private TagQualityAnalysis quality;

  private long timestamp;

  private final EsTagC2monInfo c2mon;

  private final Map<String, String> metadata = new HashMap<>();

  /**
   * Default Constructor should only be used to instantiate the fallback mechanism.
   */
  public EsTag() {
    this.id = -1L;
    this.c2mon = new EsTagC2monInfo("String");
  }

  public EsTag(long id, final String dataType) {
    this.id = id;
    this.c2mon = new EsTagC2monInfo(dataType);
    init(dataType);
  }

  private void init(final String dataType) {

    Class<?> clazz = TypeConverter.getType(dataType);
    if (clazz == null) {
      type = TYPE_OBJECT;
    }
    else if (Number.class.isAssignableFrom(clazz)) {
      type = TYPE_NUMBER;
    }
    else if (Boolean.class.isAssignableFrom(clazz)) {
      type = TYPE_BOOLEAN;
    }
    else if (String.class.isAssignableFrom(clazz)) {
      type = TYPE_STRING;
    }
    else {
      type = TYPE_OBJECT;
    }
  }

  /**
   * Set the value of this EsTagBoolean to the value of the Tag in C2MON.
   *
   * @param rawValue Object supposed to be a boolean.
   */
  public void setRawValue(final Object rawValue) {
    if (rawValue == null) {
      log.trace("setRawValue() - Value is not set (rawValue= " + rawValue + ").");
      return;
    }

    switch (type) {
    case TYPE_BOOLEAN:
      initBooleanValue(rawValue);
      break;
    case TYPE_NUMBER:
      initNumberValue(rawValue);
      break;
    default:
      initStringValue(rawValue);
      break;
    }
  }

  private void initBooleanValue(final Object rawValue) {
    if (!(rawValue instanceof Boolean)) {
      throw new IllegalArgumentException("Cannot instantiate EsTag of type boolean, because the value of tag #" + id + " is of type class=" + rawValue.getClass().getName());
    }

    valueBoolean = (Boolean) rawValue;
    this.value = valueBoolean ? 1 : 0;
  }

  private void initNumberValue(final Object rawValue) {
    if (!(rawValue instanceof Number)) {
      throw new IllegalArgumentException("Cannot instantiate EsTag of type number, because the value of tag #" + id + " is of type class=" + rawValue.getClass().getName());
    }
    this.value = (Number) rawValue;

    if (rawValue instanceof Long) {
      this.valueLong = (Long) rawValue;
    }
  }

  private void initStringValue(final Object rawValue) {
    if (!(rawValue instanceof String)) {
      throw new IllegalArgumentException("Cannot instantiate EsTag of type string, because the value of tag #" + id + " is of type class=" + rawValue.getClass().getName());
    }

    this.valueString = (String) rawValue;
  }

  @Override
  public IFallback getObject(String line) {
    return gson.fromJson(line, EsTag.class);
  }

  @Override
  public String getId() {
    return String.valueOf(id);
  }

  public long getIdAsLong() {
    return id;
  }

  /**
   * JSON representation of a AbstractEsTag.
   */
  @Override
  public String toString() {
    String json = gson.toJson(this);
    log.debug(json);
    return json;
  }

}
