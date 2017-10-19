package cern.c2mon.server.cache.loading;

import cern.c2mon.server.common.expression.ExpressionCacheObject;

/**
 * @author Martin Flamm
 */
public interface ExpressionLoaderDAO extends BatchCacheLoaderDAO<ExpressionCacheObject>, ConfigurableDAO<ExpressionCacheObject> {
}
