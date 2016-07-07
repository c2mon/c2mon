/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.client.core.configuration;

import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.configuration.api.tag.RuleTag;

import java.util.List;
import java.util.Set;

/**
 * @author Franz Ritter
 */
public interface RuleTagConfigurationManager {

  /**
   * Creates a new 'RuleTag' on the server with the given name, data type and
   * <a href="http://c2mon.web.cern.ch/c2mon/docs/#_rule_engine">rule
   * expression</a>.
   * After a successful creation the server starts to evaluate the rule based
   * on the already applied DataTags.
   * <p>
   * The RuleTag is created with default parameters.
   *
   * @param ruleExpression The
   *                       <a href="http://c2mon.web.cern.ch/c2mon/docs/#_rule_engine">
   *                       rule expression</a>.
   * @param name           The name of the RuleTag.
   * @param dataType       The data type of the RuleTag.
   * @return A {@link ConfigurationReport} containing all details of the
   * RuleTag configuration, including if it was successful or not.
   * @see RuleTagConfigurationManager#createRule(RuleTag)
   */
  ConfigurationReport createRule(String ruleExpression, String name, Class<?> dataType);

  /**
   * Creates a new 'RuleTag' on the server with the given name, data type and
   * <a href="http://c2mon.web.cern.ch/c2mon/docs/#_rule_engine">rule
   * expression</a> set in the {@link RuleTag} object. After a successful
   * creation the server starts to evaluate the rule based on the already
   * applied DataTags.
   * <p>
   * Next to the specified parameters the RuleTag is created with default
   * parameters.
   * <p>
   * Note: You have to use {@link RuleTag#create(String, Class, String)} to
   * instantiate the 'ruleTag' parameter of this method.
   *
   * @param ruleTag The {@link RuleTag} configuration for the 'create'.
   * @return A {@link ConfigurationReport} containing all details of the
   * RuleTag configuration, including if it was successful or not.
   * @see RuleTagConfigurationManager#createRule(RuleTag)
   */
  ConfigurationReport createRule(RuleTag ruleTag);

  /**
   * Creates multiple new 'RuleTags' on the server with the given names, data
   * types and <a href="http://c2mon.web.cern.ch/c2mon/docs/#_rule_engine">
   * rule expressions</a> set in the {@link RuleTag} objects. After a
   * successful creation the server starts to evaluate the rules based on the
   * already applied DataTags.
   * <p>
   * Next to the specified parameters the RuleTags are created with default
   * parameters.
   * <p>
   * Note: You have to use {@link RuleTag#create(String, Class, String)} to
   * instantiate the 'ruleTags' parameter of this method.
   *
   * @param ruleTags The {@link RuleTag} configurations for the 'create'.
   * @return A {@link ConfigurationReport} containing all details of the
   * RuleTag configuration, including if it was successful or not.
   * @see RuleTagConfigurationManager#createRule(RuleTag)
   */
  ConfigurationReport createRules(List<RuleTag> ruleTags);

  /**
   * Updates a existing {@link RuleTag} with the given parameters set in the
   * {@link RuleTag} object.
   * <p>
   * Note: You have to use one of the following methods to instantiate the
   * 'tag' parameter of this method.
   * <p>
   * {@link RuleTag#update(Long)}, {@link RuleTag#update(String)}
   *
   * @param tag The {@link RuleTag} configuration for the 'update'.
   * @return A {@link ConfigurationReport} containing all details of the
   * Process configuration, including if it was successful or not.
   */
  ConfigurationReport updateRuleTag(RuleTag tag);

  /**
   * Updates multiple existing {@link RuleTag} with the given parameters set in
   * the {@link RuleTag} objects.
   * <p>
   * Note: You have to use one of the following methods to instantiate the
   * 'tag' parameter of this method.
   * <p>
   * {@link RuleTag#update(Long)}, {@link RuleTag#update(String)}
   *
   * @param tags The list of {@link RuleTag} configurations for the 'updates'.
   * @return A {@link ConfigurationReport} containing all details of the Tag
   * configuration, including if it was successful or not.
   */
  ConfigurationReport updateRuleTags(List<RuleTag> tags);

  /**
   * Removes a existing {@link RuleTag} with the given id.
   *
   * @param id The id of the Tag which needs to be removed.
   * @return A {@link ConfigurationReport} containing all details of the Tag
   * configuration, including if it was successful or not.
   */
  ConfigurationReport removeRuleTagById(Long id);

  /**
   * Removes multiple existing {@link RuleTag} with the given ids.
   *
   * @param ids The list of ids of the Tags which needs to be removed.
   * @return A {@link ConfigurationReport} containing all details of the
   * Process configuration, including if it was successful or not.
   */
  ConfigurationReport removeRuleTagsById(Set<Long> ids);

  /**
   * Removes a existing {@link RuleTag} with the given name.
   *
   * @param name The name of the Tag which needs to be removed.
   * @return A {@link ConfigurationReport} containing all details of the
   * Process configuration, including if it was successful or not.
   */
  ConfigurationReport removeRuleTag(String name);

  /**
   * Removes multiple existing {@link RuleTag} with the given names.
   *
   * @param names The list of names of the Tags which needs to be removed.
   * @return A {@link ConfigurationReport} containing all details of the
   * Process configuration, including if it was successful or not.
   */
  ConfigurationReport removeRuleTags(Set<String> names);
}
