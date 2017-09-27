package cern.c2mon.server.cache.dbaccess;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.common.expression.DslExpression;
import cern.c2mon.server.common.metadata.Metadata;

/**
 * @author Martin Flamm
 */
public class ExpressionMapperTest extends AbstractMapperTest {

  @Autowired
  private ExpressionMapper expressionMapper;

  @Test
  public void testInsertCompletes() {
    DslExpression expression = createExpression(1L, "avg(q(name:'*/cpu.loadavg', '1m'))");

    expressionMapper.insertExpression(expression);
    expressionMapper.deleteExpression(expression.getId());
  }

  private DslExpression createExpression(long id, String expressionString) {
    DslExpression expression = new DslExpression();
    expression.setId(id);
    expression.setExpression(expressionString);

    Metadata metadata = new Metadata();
    metadata.addMetadata("responsible", "Joe Bloggs");
    expression.setMetadata(metadata);

    expression.setAlarmIds(new ArrayList<>(Arrays.asList(1L, 2L, 3L)));
    expression.setDescription("This is a expression description");
    expression.setDataType("java.lang.Float");
    expression.setVersion(12525216526L);
    expression.setResult(22.25452F);
    return expression;
  }
}