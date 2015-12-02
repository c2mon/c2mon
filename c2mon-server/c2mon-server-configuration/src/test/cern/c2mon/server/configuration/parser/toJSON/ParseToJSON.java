package cern.c2mon.server.configuration.parser.toJSON;


import cern.c2mon.shared.client.configuration.api.Configuration;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static cern.c2mon.server.configuration.parser.util.ConfigurationAllTogetherUtil.buildAllMandatoryWithMetaData;
import static org.junit.Assert.assertEquals;

/**
 * Created by fritter on 30/11/15.
 */
public class ParseToJSON {

  @Test
  public void parseComplexConfiguration() {
    Configuration insert = buildAllMandatoryWithMetaData();

    ObjectMapper mapper = new ObjectMapper();
    try {
      mapper.writeValue(new File("test.json"), insert);
      Configuration confRead = mapper.readValue(new File("test.json"), Configuration.class);

      assertEquals(confRead, insert);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
