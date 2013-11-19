package cern.c2mon.shared.client.tag;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import org.junit.Test;


public class TagConfigImplTest {


  @Test
  public void testXMLSerialization() throws Exception {
    final TagConfigImpl tc = new TagConfigImpl(1234L);        

    tc.toString();

    TagConfig tc2 = TagConfigImpl.fromXml(tc.toString());

    assertEquals(tc.getId(), tc2.getId());

    tc.addRuleIds(Arrays.asList(23123L, 45243L, 54234L));

    tc2 = TagConfigImpl.fromXml(tc.toString());

    assertArrayEquals(tc.getRuleIds().toArray(new Long[0]), tc2.getRuleIds().toArray(new Long[0]));

    tc.setHardwareAddress("<HardwareAddress class=\"test test test\"></HardwareAddress>");

    tc2 = TagConfigImpl.fromXml(tc.toString());

    assertEquals(tc.getHardwareAddress(), tc2.getHardwareAddress());

    tc.addPublication(Publisher.DIP, "dip.test.topic");
    tc.addPublication(Publisher.JAPC, "japc.test.topic");

    tc2 = TagConfigImpl.fromXml(tc.toString());

    assertEquals(tc.getDipPublication(), tc2.getDipPublication());
    assertEquals(tc.getJapcPublication(), tc2.getJapcPublication());

    tc.setAlarmIds(Arrays.asList(12123L, 13243L));

    tc2 = TagConfigImpl.fromXml(tc.toString());

    assertArrayEquals(tc2.getAlarmIds().toArray(new Long[0]), tc.getAlarmIds().toArray(new Long[0]));
  }
  
  @Test
  public void testValueDeadbandLabelIsIncluded() {
    
    final TagConfigImpl tc = new TagConfigImpl(1234L);        
    tc.setValueDeadbandType((short)1);
    tc.getXml().contains("<ValueDeadbandLabel>");
  }
}

