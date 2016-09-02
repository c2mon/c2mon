package cern.c2mon.shared.client.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Franz Ritter
 */
public class JacksonSerializer {

  public static ObjectMapper mapper = new ObjectMapper();
}
