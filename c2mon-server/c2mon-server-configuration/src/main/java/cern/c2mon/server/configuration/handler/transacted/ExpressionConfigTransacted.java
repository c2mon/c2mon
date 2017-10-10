package cern.c2mon.server.configuration.handler.transacted;

import java.util.Properties;

import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 * @author Martin Flamm
 */
public interface ExpressionConfigTransacted extends TagConfigTransacted<RuleTag> {

  /**
   * Transacted method creating an expression.
   * @param element configuration details for creation
   * @throws IllegalAccessException
   */
  void doCreateExpression(ConfigurationElement element) throws IllegalAccessException;

  /**
   * Transacted method updating an expression.
   * @param id of rule to update
   * @param properties with update info
   * @throws IllegalAccessException
   */
  void doUpdateExpression(Long id, Properties properties) throws IllegalAccessException;

  /**
   * Transacted method removing an expression. Need to confirm cache removal once
   * this returns.
   * @param id of rule to remove
   * @param elementReport report on removal
   */
  void doRemoveExpression(Long id, ConfigurationElementReport elementReport);
}
