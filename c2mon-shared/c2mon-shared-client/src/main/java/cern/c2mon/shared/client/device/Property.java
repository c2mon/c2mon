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
package cern.c2mon.shared.client.device;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.simpleframework.xml.ElementList;

/**
 * Simple XML mapper bean representing a device class property. Used when
 * deserialising device class properties during configuration.
 *
 * @author Justin Lewis Salmon
 */
@NoArgsConstructor
public class Property extends DeviceClassElement implements Serializable {

  private static final long serialVersionUID = 779255306056735769L;

  @Getter
  @ElementList(required = false, name = "Fields")
  private List<Property> fields = new ArrayList<>();

  /**
   * Default constructor.
   *
   * @param id the unique ID of the property
   * @param name the name of the property
   * @param description the property description
   */
  public Property(final Long id, final String name, final String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }

  /**
   * Constructor which will optionally set the fields of the property.
   *
   * @param id the unique ID of the property
   * @param name the name of the property
   * @param description the property description
   * @param fields the property fields
   */
  public Property(final Long id, final String name, final String description, final List<Property> fields) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.fields = fields;
  }

  /**
   * Constructor to use during property creation requests.
   *
   * @param name the name of the property
   * @param description the property description
   */
  public Property(String name, String description) {
    this.name = name;
    this.description = description;
  }

  /**
   * Retrieve the fields IDs of this property
   *
   * @return the field IDs
   */
  public List<Long> getFieldIds() {
    List<Long> fieldIds = new ArrayList<>();

    if (fields != null) {
      for (Property field : fields) {
        if (field.getId() != null) {
          fieldIds.add(field.getId());
        }
      }
    }

    return fieldIds;
  }
}
