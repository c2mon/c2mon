package cern.c2mon.server.cache.dbaccess;

import cern.c2mon.server.common.expression.ExpressionCacheObject;

/**
 * Mapper for accessing and updating the rule expression
 *
 * @author Martin Flamm
 */
public interface ExpressionMapper extends BatchLoaderMapper<ExpressionCacheObject>, PersistenceMapper<ExpressionCacheObject>, LoaderMapper<ExpressionCacheObject>, ConfigurableMapper<ExpressionCacheObject> {

  void insertExpression(ExpressionCacheObject expression);

  void deleteExpression(long expressionId);
}