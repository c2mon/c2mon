package cern.c2mon.shared.client.serialize;

import cern.c2mon.shared.client.tag.TagConfigImpl;
import cern.c2mon.shared.common.datatag.address.SimpleHardwareAddress;
import cern.c2mon.shared.common.datatag.address.impl.SimpleHardwareAddressImpl;
import cern.c2mon.shared.util.json.GsonFactory;
import com.google.gson.Gson;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by fritter on 08/07/16.
 */
public class TagConfig {

  private static final Gson GSON = GsonFactory.createGson();

  @Test
  public void serializeTagConfigWithHardwareAddress() {
    TagConfigImpl tagConfig = createTagConfig();
    tagConfig.setHardwareAddress(new SimpleHardwareAddressImpl("address").toConfigXML());

    String jsonString = GSON.toJson(tagConfig);
    TagConfigImpl tagConfigFromJson = GSON.fromJson(jsonString, TagConfigImpl.class);

    assertEquals(tagConfig, tagConfigFromJson);


  }

  @Test
  public void serializeTagConfigWithAddressParameters() {
    TagConfigImpl tagConfig = createTagConfig();
    Map<String, String> addressParameters = new HashMap<>();
    addressParameters.put("key", "value");
    tagConfig.setAddressParameters(addressParameters);

    String jsonString = GSON.toJson(tagConfig);
    TagConfigImpl tagConfigFromJson = GSON.fromJson(jsonString, TagConfigImpl.class);

    assertEquals(tagConfig, tagConfigFromJson);
  }

  private static TagConfigImpl createTagConfig() {
    TagConfigImpl tag = new TagConfigImpl(1L);
    tag.setAlarmIds(new ArrayList<Long>(tag.getAlarmIds()));
    tag.setControlTag(Boolean.TRUE);
    tag.setMinValue("0");
    tag.setMaxValue("10");
    tag.setValueDeadbandType((short) 0);
    tag.setValueDeadband(10.0f);
    tag.setTimeDeadband(1000);
    tag.setGuaranteedDelivery(Boolean.TRUE);
    tag.setPriority(0);

    return tag;
  }
}
