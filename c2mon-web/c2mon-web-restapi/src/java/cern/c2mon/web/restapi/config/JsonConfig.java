package cern.c2mon.web.restapi.config;

import cern.c2mon.client.core.tag.ClientCommandTagImpl;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.client.ext.history.updates.HistoryTagValueUpdateImpl;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;
import cern.c2mon.web.restapi.serialization.AlarmValueSerializer;
import cern.c2mon.web.restapi.serialization.ClientCommandTagSerializer;
import cern.c2mon.web.restapi.serialization.ClientDataTagSerializer;
import cern.c2mon.web.restapi.serialization.HistoryTagValueUpdateSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
public class JsonConfig {

  @Bean
  public Jackson2ObjectMapperBuilder jacksonBuilder() {
    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

    // Add custom serializers here
    builder.serializerByType(AlarmValueImpl.class, new AlarmValueSerializer());
    builder.serializerByType(ClientDataTagImpl.class, new ClientDataTagSerializer());
    builder.serializerByType(ClientCommandTagImpl.class, new ClientCommandTagSerializer());
    builder.serializerByType(HistoryTagValueUpdateImpl.class, new HistoryTagValueUpdateSerializer());

    // Enable pretty printing
    builder.indentOutput(true);

    return builder;
  }
}
