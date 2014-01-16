package cern.c2mon.server.configuration.handler.transacted;

import java.util.Properties;

import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 * For internal use only. Allows use of Spring AOP for transaction management.
 * 
 * @author Mark Brightwell
 *
 */
public interface RuleTagConfigTransacted extends TagConfigTransacted<RuleTag> {

  /**
   * Transacted method creating a rule tag.
   * @param element configuration details for creation
   * @throws IllegalAccessException
   */
  void doCreateRuleTag(ConfigurationElement element) throws IllegalAccessException;

  /**
   * Transacted method updating a rule tag.
   * @param id of rule to update
   * @param properties with update info
   * @throws IllegalAccessException
   */
  void doUpdateRuleTag(Long id, Properties properties) throws IllegalAccessException;

  /**
   * Transacted method removing a rule tag. Need to confirm cache removal once
   * this returns.
   * @param id of rule to remove
   * @param elementReport report on removal
   */
  void doRemoveRuleTag(Long id, ConfigurationElementReport elementReport);

}
