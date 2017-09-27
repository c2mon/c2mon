package cern.c2mon.server.cache.dbaccess;

import cern.c2mon.server.common.expression.DslExpression;

/**
 * Mapper for accessing and updating the rule expression
 *
 * @author Martin Flamm
 */
public interface ExpressionMapper extends PersistenceMapper<DslExpression>, LoaderMapper<DslExpression>,
    BatchLoaderMapper<DslExpression>, ConfigurableMapper<DslExpression> {

  void insertExpression(DslExpression expression);

  void deleteExpression(long expressionId);
}