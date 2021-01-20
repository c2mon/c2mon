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
package cern.c2mon.client.core.device.property;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.common.tag.TypeNumeric;
import cern.c2mon.client.core.device.Device;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.type.TypeConverter;
import cern.c2mon.shared.rule.RuleExpression;

 /**
 * This class represents a constant value object, which is used as a static property of a {@link Device}.
 *
 * @param <T> The type of the value
 * @author Justin Lewis Salmon
 */
public class ClientConstantValue<T> implements Tag {

  /**
   * The ID of this tag.
   */
  private Long id;

  /**
   * The actual constant value.
   */
  private final T value;

  /**
   * The type of the value.
   */
  private final Class<T> resultType;

  /**
   * Default constructor.
   *
   * @param value the constant value
   * @param resultType the type of the constant value
   */
  public ClientConstantValue(final T value, final Class<T> resultType) {
    if (value == null) {
      throw new NullPointerException("ClientConstantValue cannot be instantiated with null value argument");
    }

    this.id = -1L;
    this.value = value;

    if (resultType == null) {
      this.resultType = (Class<T>) String.class;
    } else {
      this.resultType = resultType;
    }
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public DataTagQuality getDataTagQuality() {
    return null;
  }

  @Override
  public T getValue() {
    return TypeConverter.castToType(value, resultType);
  }

  @Override
  public String getValueDescription() {
    return null;
  }

  @Override
  public String getUnit() {
    return null;
  }

  @Override
  public TagMode getMode() {
    return null;
  }

  @Override
  public boolean isSimulated() {
    return false;
  }

  @Override
  public Collection<AlarmValue> getAlarms() {
    return Collections.emptyList();
  }

  @Override
  public Collection<Long> getAlarmIds() {
    return Collections.emptyList();
  }

  @Override
  public Collection<Long> getEquipmentIds() {
    return Collections.emptyList();
  }

  @Override
  public Collection<Long> getSubEquipmentIds() {
    return Collections.emptyList();
  }

  @Override
  public Collection<Long> getProcessIds() {
    return Collections.emptyList();
  }

  @Override
  public boolean isRuleResult() {
    return false;
  }

  @Override
  public RuleExpression getRuleExpression() {
    return null;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public Timestamp getTimestamp() {
    return null;
  }

  @Override
  public Timestamp getDaqTimestamp() {
    return null;
  }

  @Override
  public Timestamp getServerTimestamp() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public Class<?> getType() {
    return resultType;
  }

  @Override
  public TypeNumeric getTypeNumeric() {
    Class< ? > type = getType();
    if (type != null) {
      int typeNumeric = type.hashCode();
      for (TypeNumeric t : TypeNumeric.values()) {
        if (t.getCode() == typeNumeric) {
          return t;
        }
      }
    }
    
    return TypeNumeric.TYPE_UNKNOWN;
  }

  @Override
  public Map<String, Object> getMetadata() {
    return null;
  }

  @Override
  public boolean isAliveTag() {
    return false;
  }

  @Override
  public boolean isControlTag() {
    return false;
  }

}
