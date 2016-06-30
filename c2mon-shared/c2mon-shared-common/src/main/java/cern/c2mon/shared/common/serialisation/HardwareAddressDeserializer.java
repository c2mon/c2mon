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

package cern.c2mon.shared.common.serialisation;

import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddressFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 *
 * Defines to Deserialize a HardwareAddress for the Jackson parser
 *
 * @author Franz Ritter
 */
public class HardwareAddressDeserializer extends com.fasterxml.jackson.databind.JsonDeserializer<HardwareAddress> {

  @Override
  public HardwareAddress deserialize(com.fasterxml.jackson.core.JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    String xmlData = node.get("xmlData").asText();

    return HardwareAddressFactory.getInstance().fromConfigXML(xmlData);
  }

  // TODO: use for not using HardwareAddress fromXML
  private <T extends HardwareAddress> T hashMapToData(HashMap<String, Object> data, String typeName) {

    HashMap<String, Object> result = new HashMap<>();

    try {
      Class<T> type = (Class<T>) Class.forName(typeName);

      T obj = type.getConstructor().newInstance();

      BeanInfo info = Introspector.getBeanInfo(type);
      PropertyDescriptor[] props = info.getPropertyDescriptors();

      for (PropertyDescriptor pd : props) {
        if (!pd.getName().equals("class")
            && pd.getReadMethod().invoke(obj) != null
            && pd.getWriteMethod().invoke(obj) != null) {

          Object fieldValue = data.get(pd.getName());

          pd.getWriteMethod().invoke(obj, fieldValue);
        }
      }

      return obj;

    } catch (IntrospectionException
        | InvocationTargetException
        | IllegalAccessException
        | ClassNotFoundException
        | InstantiationException
        | NoSuchMethodException e) {
      e.printStackTrace();

      return null;
    }
  }
}
