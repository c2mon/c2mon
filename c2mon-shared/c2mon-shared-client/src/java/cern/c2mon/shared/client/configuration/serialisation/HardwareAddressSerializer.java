/*
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
 */

package cern.c2mon.shared.client.configuration.serialisation;

import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 *
 * Defines to Serialize a HardwareAddress for the Jackson parser
 *
 * @author Franz Ritter
 */
public class HardwareAddressSerializer extends com.fasterxml.jackson.databind.JsonSerializer<HardwareAddress> {

  @Override
  public void serialize(HardwareAddress hardwareAddress, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
    jsonGenerator.writeStartObject();
    jsonGenerator.writeStringField("xmlData", hardwareAddress.toConfigXML());
    jsonGenerator.writeEndObject();
  }

  // TODO: use for not using HardwareAddress toXML
  private <T extends HardwareAddress> HashMap<String, Object> dataToHashMap(T address, Class<T> typeName) {
    HashMap<String, Object> result = new HashMap<>();

    try {
      T obj = typeName.cast(address);

      BeanInfo info = Introspector.getBeanInfo(typeName);
      PropertyDescriptor[] props = info.getPropertyDescriptors();

      for (PropertyDescriptor pd : props) {
        if (!pd.getName().equals("class")
            && pd.getReadMethod().invoke(obj) != null
            && pd.getWriteMethod().invoke(obj) != null) {
          Object value = pd.getReadMethod().invoke(obj);
          result.put(pd.getName(), value);
        }
      }

    } catch (IntrospectionException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return result;
  }

}
