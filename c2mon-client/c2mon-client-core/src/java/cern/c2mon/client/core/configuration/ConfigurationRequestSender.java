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
package cern.c2mon.client.core.configuration;

import cern.c2mon.client.common.listener.ClientRequestReportListener;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.util.jms.JmsSender;
import com.google.gson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;

/**
 * JMS sender class for sending the Configuration request to the server and waiting for the response.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class ConfigurationRequestSender {

  @Autowired
  private JmsSender jmsSender;

  @Value("${jms.config.destination}")
  private String jmsConfigDestination;

  /**
   * @param configuration
   * @param listener
   *
   * @return
   */
  public ConfigurationReport applyConfiguration(Configuration configuration, ClientRequestReportListener listener) {
    Gson gson = new GsonBuilder().registerTypeAdapter(HardwareAddress.class, new InterfaceAdapter<HardwareAddress>()).create();
    String message = gson.toJson(configuration);
    String reply = jmsSender.sendRequestToQueue(message, jmsConfigDestination, 3600000);
    return gson.fromJson(reply, ConfigurationReport.class);
  }

  /**
   * Wraps the JSON serialisation/deserialisation of an interface type, adding concrete class information
   *
   * @param <T>
   */
  final class InterfaceAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {
    @Override
    public JsonElement serialize(T object, Type interfaceType, JsonSerializationContext context) {
      final JsonObject wrapper = new JsonObject();
      wrapper.addProperty("class", object.getClass().getName());
      wrapper.add("data", context.serialize(object));
      return wrapper;
    }

    @Override
    public T deserialize(JsonElement elem, Type interfaceType, JsonDeserializationContext context) throws JsonParseException {
      final JsonObject wrapper = (JsonObject) elem;
      final JsonElement typeName = get(wrapper, "class");
      final JsonElement data = get(wrapper, "data");
      final Type actualType = typeForName(typeName);
      return context.deserialize(data, actualType);
    }

    private Type typeForName(final JsonElement typeElem) {
      try {
        return Class.forName(typeElem.getAsString());
      } catch (ClassNotFoundException e) {
        throw new JsonParseException(e);
      }
    }

    private JsonElement get(final JsonObject wrapper, String memberName) {
      final JsonElement elem = wrapper.get(memberName);
      if (elem == null) throw new JsonParseException("no '" + memberName + "' member found in what was expected to be an interface wrapper");
      return elem;
    }
  }
}

