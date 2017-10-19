package cern.c2mon.server.cache.dbaccess;

import java.util.ArrayList;
import java.util.Arrays;

import javax.annotation.Resource;

import org.junit.Test;

import cern.c2mon.server.common.expression.ExpressionCacheObject;
import cern.c2mon.server.common.metadata.Metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Martin Flamm
 */
public class ExpressionMapperTest extends AbstractMapperTest {

  @Resource
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
    assertEquals("name_" + 1L, cachedObject.getName());

    expressionMapper.deleteExpression(expression.getId());
  }

  @Test
  public void testDeleteCompletes() {
    ExpressionCacheObject expression = createExpression(1L, "avg(q(name:'*/cpu.loadavg', '1m'))");
    expressionMapper.insertExpression(expression);

    expressionMapper.deleteExpression(1L);
    assertTrue(expressionMapper.getAll().isEmpty());
  }

  @Test
  public void testGetNumberItems() {
    ExpressionCacheObject expression;

    expression = createExpression(1L, "avg(q(name:'*/cpu.loadavg', '1m'))");
    expressionMapper.insertExpression(expression);
    expression = createExpression(2L, "avg(q(name:'*/cpu.loadavg', '1m'))");
    expressionMapper.insertExpression(expression);
    expression = createExpression(3L, "avg(q(name:'*/cpu.loadavg', '1m'))");
    expressionMapper.insertExpression(expression);

    Integer numberItems = expressionMapper.getNumberItems();
    assertTrue(numberItems == 3);
    expressionMapper.deleteExpression(1L);
    expressionMapper.deleteExpression(2L);
    expressionMapper.deleteExpression(3L);
  }

  private ExpressionCacheObject createExpression(long id, String expressionString) {
    ExpressionCacheObject expression = new ExpressionCacheObject(id);
    expression.setExpression(expressionString);
    expression.setName("name_" + id);

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