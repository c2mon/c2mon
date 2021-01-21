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

import java.util.*;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Simple XML mapper bean representing a list of device class properties. Used
 * when deserialising device class properties during configuration.
 *
 * @author Justin Lewis Salmon
 */
@Root(name = "Properties")
public class PropertyList implements DeviceClassOrDeviceSerializableElement {

  @ElementList(entry = "Property", inline = true, required = false)
  private Collection<Property> properties = new HashSet<>();

  /**
   * Create a new wrapper bean for the referenced properties
   * @param properties the  properties to wrap
   */
  public PropertyList(Collection<Property> properties) {
    this.properties = properties;
  }

  /**
   * Default constructor used during deserialization
   */
  public PropertyList() {
    super();
  }

  /**
   * Get the unwrapped properties in a mutable list
   * @return the unwrapped properties in a mutable list
   */
  public List<Property> getProperties() {
    return new ArrayList<>(properties);
  }
}
