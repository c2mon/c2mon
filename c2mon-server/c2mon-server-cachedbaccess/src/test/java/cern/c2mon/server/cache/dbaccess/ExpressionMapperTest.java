package cern.c2mon.server.cache.dbaccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.common.expression.ExpressionCacheObject;
import cern.c2mon.server.common.metadata.Metadata;

import static org.junit.Assert.*;

/**
 * @author Martin Flamm
 */
public class ExpressionMapperTest extends AbstractMapperTest {

  @Autowired
  private ExpressionMapper expressionMapper;

  @Test
  public void testInsertCompletes() {
    ExpressionCacheObject expression = createExpression(1L, "avg(q(name:'*/cpu.loadavg', '1m'))");
    expressionMapper.insertExpression(expression);

    ExpressionCacheObject cachedObject = expressionMapper.getItem(1L);

    assertEquals("avg(q(name:'*/cpu.loadavg', '1m'))", cachedObject.getExpression());
    Metadata metadata = new Metadata();
    metadata.addMetadata("responsible", "Joe Bloggs");
    assertEquals(metadata, cachedObject.getMetadata());
    assertTrue(1L == cachedObject.getId());
    //assertEquals(Arrays.asList(1L, 2L, 3L), cachedObject.getAlarmIds());
    assertEquals("This is a expression description", cachedObject.getDescription());
    assertEquals("Java.lang.Float", cachedObject.getDataType());
    assertEquals(1252516526L, cachedObject.getVersion());
    assertEquals(22.25452D, cachedObject.getValue());

    expressionMapper.deleteExpression(expression.getId());
  }

  @Test
  public void testDeleteCompletes() {
    ExpressionCacheObject expression = createExpression(1L, "avg(q(name:'*/cpu.loadavg', '1m'))");
    expressionMapper.insertExpression(expression);

    expressionMapper.deleteExpression(expression.getId());
    assertTrue(expressionMapper.getAll().isEmpty());
  }

  private ExpressionCacheObject createExpression(long id, String expressionString) {
    ExpressionCacheObject expression = new ExpressionCacheObject(id);
    expression.setExpression(expressionString);

    Metadata metadata = new Metadata();
    metadata.addMetadata("responsible", "Joe Bloggs");
    expression.setMetadata(metadata);

    expression.setAlarmIds(new ArrayList<>(Arrays.asList(1L, 2L, 3L)));
    expression.setDescription("This is a expression description");
    expression.setDataType("Java.lang.Float");
    expression.setVersion(1252516526L);
    expression.setValue(22.25452F);

    return expression;
  }
}