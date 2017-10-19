package cern.c2mon.server.cache.loading.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.dbaccess.ExpressionMapper;
import cern.c2mon.server.cache.loading.ExpressionLoaderDAO;
import cern.c2mon.server.cache.loading.common.AbstractBatchLoaderDAO;
import cern.c2mon.server.common.expression.ExpressionCacheObject;

/**
 * @author Martin Flamm
 */
@Service("expressionLoaderDAO")
public class ExpressionLoaderDAOImpl extends AbstractBatchLoaderDAO<ExpressionCacheObject> implements ExpressionLoaderDAO {

  private ExpressionMapper expressionMapper;

  @Autowired
  public ExpressionLoaderDAOImpl(ExpressionMapper expressionMapper) {
    super(expressionMapper);
    this.expressionMapper = expressionMapper;
  }

  @Override
  public void deleteItem(Long id) {
    expressionMapper.deleteExpression(id);
  }

  @Override
  public void updateConfig(ExpressionCacheObject expression) {
    expressionMapper.updateConfig(expression);
  }

  @Override
  public void insert(ExpressionCacheObject expression) {
    expressionMapper.insertExpression(expression);
  }

  @Override
  protected ExpressionCacheObject doPostDbLoading(ExpressionCacheObject item) {
    return item;
  }

  @Override
  public ExpressionCacheObject getItem(Object id) {
    return expressionMapper.getItem(id);
  }

  @Override
  public boolean isInDb(Long id) {
    return expressionMapper.isInDb(id);
  }
}
