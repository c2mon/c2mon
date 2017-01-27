package cern.c2mon.server.elasticsearch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(JUnit4.class)
public class MappingFactoryTests {

  @Test
  public void alarmMapping() {
    assertNotNull(MappingFactory.createAlarmMapping());
  }

  @Test
  public void supervisionMapping() {
    assertNotNull(MappingFactory.createSupervisionMapping());
  }

  @Test
  public void booleanTagMapping() {
    String mapping = MappingFactory.createTagMapping(Boolean.class.getName());
    assertTrue(mapping.contains("value"));
    assertTrue(mapping.contains("valueBoolean"));
  }

  @Test
  public void shortTagMapping() {
    String mapping = MappingFactory.createTagMapping(Short.class.getName());
    assertTrue(mapping.contains("\"value\":{\"type\":\"double\"}"));
  }

  @Test
  public void integerTagMapping() {
    String mapping = MappingFactory.createTagMapping(Integer.class.getName());
    assertTrue(mapping.contains("\"value\":{\"type\":\"double\"}"));
  }

  @Test
  public void floatTagMapping() {
    String mapping = MappingFactory.createTagMapping(Float.class.getName());
    assertTrue(mapping.contains("\"value\":{\"type\":\"double\"}"));
  }

  @Test
  public void doubleTagMapping() {
    String mapping = MappingFactory.createTagMapping(Boolean.class.getName());
    assertTrue(mapping.contains("\"value\":{\"type\":\"double\"}"));
  }

  @Test
  public void longTagMapping() {
    String mapping = MappingFactory.createTagMapping(Long.class.getName());
    assertTrue(mapping.contains("\"value\":{\"type\":\"double\"}"));
    assertTrue(mapping.contains("\"valueLong\":{\"type\":\"long\"}"));
  }

  @Test
  public void stringTagMapping() {
    String mapping = MappingFactory.createTagMapping(String.class.getName());
    assertTrue(mapping.contains("\"valueString\":{\"type\":\"string\",\"index\":\"not_analyzed\"}"));
  }

  @Test
  public void objectTagMapping() {
    String mapping = MappingFactory.createTagMapping(Object.class.getName());
    assertTrue(mapping.contains("\"valueObject\":{\"type\":\"nested\",\"dynamic\":\"true\"}"));
  }

  @Test
  public void unknownTypeTagMapping() {
    String mapping = MappingFactory.createTagMapping("com.foo.UnknownType");
    assertTrue(mapping.contains("\"valueObject\":{\"type\":\"nested\",\"index\":\"analyzed\"}"));
  }
}
