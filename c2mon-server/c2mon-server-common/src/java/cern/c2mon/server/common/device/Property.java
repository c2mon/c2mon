/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2014 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.server.common.device;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

/**
 * Simple XML mapper bean representing a device class property. Used when
 * deserialising device class properties during configuration.
 *
 * @author Justin Lewis Salmon
 */
public class Property implements Serializable {

  private static final long serialVersionUID = 779255306056735769L;

  @Attribute
  private Long id;

  @Attribute
  private String name;

  @Element(required = false)
  private String description;

  /**
   * The list of nested fields of this property.
   */
  @ElementList(required = false, name = "Fields")
  private List<Property> fields;

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

  public Property() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public List<Property> getFields() {
    return fields;
  }

  public List<Long> getFieldIds() {
    List<Long> fieldIds = new ArrayList<>();

    if (this.fields != null) {
      for (Property field : fields) {
        if (field.getId() != null) {
          fieldIds.add(field.getId());
        }
      }
    }

    return fieldIds;
  }
}
