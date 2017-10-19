package cern.c2mon.server.configuration.handler.transacted;

import java.util.Collection;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.cache.ExpressionFacade;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.RuleTagFacade;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.cache.loading.ExpressionLoaderDAO;
import cern.c2mon.server.cache.loading.RuleTagLoaderDAO;
import cern.c2mon.server.common.expression.ExpressionCacheObject;
import cern.c2mon.server.common.listener.ConfigurationEventListener;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.dsl.ExpressionCache;
import cern.c2mon.server.dsl.ExpressionParser;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 * @author Martin Flamm
 */
@Slf4j
@Service
public class ExpressionConfigTransactedImpl extends TagConfigTransactedImpl<RuleTag> implements ExpressionConfigTransacted {

  private ExpressionLoaderDAO expressionLoaderDAO;
  private ExpressionFacade expressionFacade;
  private ExpressionParser compiler;

  @Autowired
  public ExpressionConfigTransactedImpl(RuleTagCache ruleTagCache,
                                        RuleTagFacade ruleTagFacade,
                                        RuleTagLoaderDAO ruleTagLoaderDAO,
                                        ExpressionLoaderDAO expressionLoaderDAO,
                                        ExpressionFacade expressionFacade,
                                        TagLocationService tagLocationService,
                                        ExpressionParser expressionParser,
                                        GenericApplicationContext context) {
    super(ruleTagLoaderDAO, ruleTagFacade, ruleTagCache, tagLocationService, context);
    this.expressionLoaderDAO = expressionLoaderDAO;
    this.expressionFacade = expressionFacade;
    this.compiler = expressionParser;
  }

  @Override
  @Transactional(value = "cacheTransactionManager")
  public void doCreateExpression(ConfigurationElement element) throws IllegalAccessException {

    checkId(element.getEntityId());

    tagCache.acquireWriteLockOnKey(element.getEntityId());
    try {
      log.trace("Creating expression with id {}", element.getEntityId());
      ExpressionCacheObject expression = (ExpressionCacheObject) expressionFacade.createCacheObject(element.getEntityId(), element.getElementProperties());
      try {
        expressionLoaderDAO.insert(expression);
      } catch (Exception e) {
        log.error("Exception caught while inserting a new Rule into the DB - rolling back changes", e);
        throw new UnexpectedRollbackException("Unexpected exception while creating a Rule: rolling back the change", e);
      }
      try {
        ExpressionCache.cacheCompiledExpression(expression.getId(), compiler.compileExpression(expression));
        ExpressionCache.cacheExpressionId(expression.getId());

        for (ConfigurationEventListener listener : configurationEventListeners) {
          listener.onConfigurationEvent(expression, ConfigConstants.Action.CREATE);
        }

        tagCache.putQuiet(expression);
      } catch (RuntimeException e) {
        String errMessage = "Exception caught while adding a Expression - rolling back DB transaction.";
        log.error(errMessage, e);
        tagCache.remove(expression.getId());
        throw new UnexpectedRollbackException(errMessage, e);
      }
    } finally {
      tagCache.releaseWriteLockOnKey(element.getEntityId());
    }
  }

  @Override
  public void doUpdateExpression(Long id, Properties properties) throws IllegalAccessException {

  }

  @Override
  public void doRemoveExpression(Long id, ConfigurationElementReport elementReport) {

  }
}
