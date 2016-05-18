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
package cern.c2mon.server.configuration.impl;

import java.io.IOException;
import java.lang.reflect.Type;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.serialisation.HardwareAddressDeserializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.stereotype.Service;

import cern.c2mon.server.configuration.ConfigurationLoader;
import cern.c2mon.shared.client.configuration.ConfigurationException;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;

/**
 * Handles configuration requests received on JMS from clients.
 *
 * <p>
 * The request is processed and a configuration report is returned to the sender
 * (in the form of an XML message on a reply topic specified by the client).
 *
 * @author Mark Brightwell
 * @author Justin Lewis Salmon
 *
 */
@Service("configurationRequestHandler")
public class ConfigurationRequestHandler implements SessionAwareMessageListener<Message> {

  /**
   * Private class logger.
   */
  private static final Logger log = LoggerFactory.getLogger(ConfigurationRequestHandler.class);

  /**
   * Reference to the configuration loader.
   */
  @Autowired
  private ConfigurationLoader configurationLoader;

  @Override
  public void onMessage(Message message, Session session) throws JMSException {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addDeserializer(HardwareAddress.class, new HardwareAddressDeserializer());
    mapper.registerModule(module);
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    ConfigurationReport configurationReport;
    try {
      Configuration configuration = mapper.readValue(TextMessage.class.cast(message).getText(),Configuration.class);

    log.info(String.format("Configuration request received"));

      configurationReport = configurationLoader.applyConfiguration(configuration);
    } catch (ConfigurationException ex) {
      configurationReport = ex.getConfigurationReport();
    } catch (IOException e) {
      configurationReport = new ConfigurationReport();
      configurationReport.setExceptionTrace(e);
      configurationReport.setStatus(ConfigConstants.Status.FAILURE);
      configurationReport.setStatusDescription("Serialization or Deserialization of Configuration on Server side failed");
    }

    // Extract reply topic
    Destination replyDestination = null;
    try {
      replyDestination = message.getJMSReplyTo();
    } catch (JMSException jmse) {
      log.error("onMessage() : Cannot extract ReplyTo from message.", jmse);
      throw jmse;
    }

    if (replyDestination != null) {
      MessageProducer messageProducer = null;
      try {
        messageProducer = session.createProducer(replyDestination);
        TextMessage replyMessage = session.createTextMessage();
        replyMessage.setText(mapper.writeValueAsString(configurationReport));
        if (log.isDebugEnabled()) {
          log.debug("Sending reconfiguration report to client.");
        }
        log.info("Sending reconfiguration report to client.");
        messageProducer.send(replyMessage);
      } catch (JsonProcessingException e) {
        log.error("error while serializing the configurationReport: "+ e.getMessage());
      } finally {
        if (messageProducer != null) {
            messageProducer.close();
        }
      }
    } else {
      log.error("onMessage(): JMSReplyTo destination is null - cannot send reply.");
      throw new MessageConversionException("JMS reply queue could not be extracted (returned null).");
    }
  }

  /**
   * TODO: move this to shared library
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
      if (elem == null)
        throw new JsonParseException("no '" + memberName + "' member found in what was expected to be an interface wrapper");
      return elem;
    }
  }
}
