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
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.common.serialisation.HardwareAddressSerializer;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.util.jms.JmsSender;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * JMS sender class for sending the Configuration request to the server and waiting for the response.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class ConfigurationRequestSender {

  private final long DEFAULT_TIMEOUT = 60_000l; // 1 minute

  @Autowired
  private JmsSender jmsSender;

  @Autowired
  private Environment environment;

  private ObjectMapper mapper;

  public ConfigurationRequestSender() {
    mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(HardwareAddress.class, new HardwareAddressSerializer());
    mapper.registerModule(module);
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  /**
   * @param configuration
   * @param listener
   *
   * @return
   */
  public ConfigurationReport applyConfiguration(Configuration configuration, ClientRequestReportListener listener) {
    try {
      String message = mapper.writeValueAsString(configuration);
      String destination = environment.getRequiredProperty("c2mon.client.jms.config.queue");
      String reply = jmsSender.sendRequestToQueue(message, destination, DEFAULT_TIMEOUT);

      return mapper.readValue(reply, ConfigurationReport.class);

    } catch (IOException e) {

      ConfigurationReport failureReport = new ConfigurationReport();
      failureReport.setExceptionTrace(e);
      failureReport.setStatus(ConfigConstants.Status.FAILURE);
      failureReport.setStatusDescription("Serialization or Deserialization of Configuration on Client side failed");

      return failureReport;

    }
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

