package cern.c2mon.server.configuration.handler.impl;

import java.util.Properties;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.configuration.handler.ExpressionConfigHandler;
import cern.c2mon.server.configuration.handler.transacted.ExpressionConfigTransacted;
import cern.c2mon.server.configuration.handler.transacted.RuleTagConfigTransacted;
import cern.c2mon.server.configuration.impl.ConfigurationUpdateImpl;
import cern.c2mon.server.rule.RuleEvaluator;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 * @author Martin Flamm
 */
@Slf4j
@Service
public class ExpressionConfigHandlerImpl implements ExpressionConfigHandler {

  private RuleTagCache ruleTagCache;
  private RuleEvaluator ruleEvaluator;

  /**
   * Helper class for accessing the List of registered listeners
   * for configuration updates.
   */
  private ConfigurationUpdateImpl configurationUpdateImpl;

  /**
   * Transacted bean.
   */
  @Autowired
  private ExpressionConfigTransacted expressionConfigTransacted;

  @Autowired
  public ExpressionConfigHandlerImpl(final RuleTagCache ruleTagCache, final RuleEvaluator ruleEvaluator, final ConfigurationUpdateImpl configurationUpdateImpl) {
    this.ruleTagCache = ruleTagCache;
    this.ruleEvaluator = ruleEvaluator;
    this.configurationUpdateImpl = configurationUpdateImpl;
  }

  @Override
  public void createExpression(ConfigurationElement element) throws IllegalAccessException {
    ruleTagCache.acquireWriteLockOnKey(element.getEntityId());
    try {
      expressionConfigTransacted.doCreateExpression(element);
      log.trace("createExpression - Notifying Configuration update listeners");
      this.configurationUpdateImpl.notifyListeners(element.getEntityId());
    } finally {
      ruleTagCache.releaseWriteLockOnKey(element.getEntityId());
    }
  }

  @Override
  public void updateExpression(Long id, Properties elementProperties) {}

  @Override
  public void removeExpression(Long id, ConfigurationElementReport tagReport) {}

  @Override
  public void addAlarmToTag(Long tagId, Long alarmId) {
    expressionConfigTransacted.addAlarmToTag(tagId, alarmId);
  }

  @Override
  public void addRuleToTag(Long tagId, Long ruleId) {
    expressionConfigTransacted.addRuleToTag(tagId, ruleId);
  }

  @Override
  public void removeAlarmFromTag(Long tagId, Long alarmId) {
    expressionConfigTransacted.removeAlarmFromTag(tagId, alarmId);
  }

  @Override
  public void removeRuleFromTag(Long tagId, Long ruleId) {
    expressionConfigTransacted.removeRuleFromTag(tagId, ruleId);
  }
}
