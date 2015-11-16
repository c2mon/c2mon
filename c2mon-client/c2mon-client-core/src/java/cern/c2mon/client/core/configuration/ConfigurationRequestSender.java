/**
 * ****************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2011 CERN. This program is free software; you can
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
 * ****************************************************************************
 */
package cern.c2mon.client.core.configuration;

import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.ConfigurationListener;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.util.jms.JmsSender;
import com.google.gson.*;
import org.springframework.beans.factory.annotation.Autowired;
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

  /**
   * @param configuration
   * @param listener
   *
   * @return
   */
  public ConfigurationReport applyConfiguration(Configuration configuration, ConfigurationListener listener) {
    Gson gson = new GsonBuilder().registerTypeAdapter(HardwareAddress.class, new InterfaceAdapter<HardwareAddress>()).create();
    String message = gson.toJson(configuration);
    String reply = jmsSender.sendRequestToQueue(message, "c2mon.config", 3600000);
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

