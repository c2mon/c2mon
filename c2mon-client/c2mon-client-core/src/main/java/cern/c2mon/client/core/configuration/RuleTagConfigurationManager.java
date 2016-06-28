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
import cern.c2mon.shared.client.configuration.api.tag.RuleTag;

import java.util.List;

/**
 * @author Franz Ritter
 */
public interface RuleTagConfigurationManager {

  /**
   * Creates a new 'RuleTag' on the server with the given name, data type and <a href="http://c2mon.web.cern.ch/c2mon/docs/#_rule_engine">rule expression</a>.
   * After a successful creation the server starts to evaluate the rule based on the already applied DataTags.
   * <br/>
   * The RuleTag is created with default parameters.
   *
   * @param ruleExpression The <a href="http://c2mon.web.cern.ch/c2mon/docs/#_rule_engine">rule expression</a>.
   * @param name           The name of the RuleTag.
   * @param dataType       The data type of the RuleTag.
   * @return A {@link ConfigurationReport} containing all details of the RuleTag configuration,
   * including if it was successful or not.
   * @see RuleTagConfigurationManager#createRule(RuleTag)
   */
  ConfigurationReport createRule(String ruleExpression, String name, Class<?> dataType);

  /**
   * Creates a new 'RuleTag' on the server with the given name, data type and <a href="http://c2mon.web.cern.ch/c2mon/docs/#_rule_engine">rule expression</a>
   * set in the {@link RuleTag} object.
   * After a successful creation the server starts to evaluate the rule based on the already applied DataTags.
   * <br/>
   * Next to the specified parameters the RuleTag is created with default parameters.
   * <p>
   * Note: You have to use {@link RuleTag#create(String, Class, String)} to instantiate the 'ruleTag' parameter of this method.
   * </p>
   *
   * @param ruleTag The {@link RuleTag} configuration for the 'create'.
   * @return A {@link ConfigurationReport} containing all details of the RuleTag configuration,
   * including if it was successful or not.
   * @see RuleTagConfigurationManager#createRule(RuleTag)
   */
  ConfigurationReport createRule(RuleTag ruleTag);

  /**
   * Creates multiple new 'RuleTags' on the server with the given names, data types and <a href="http://c2mon.web.cern.ch/c2mon/docs/#_rule_engine">rule expressions</a>
   * set in the {@link RuleTag} objects.
   * After a successful creation the server starts to evaluate the rules based on the already applied DataTags.
   * <br/>
   * Next to the specified parameters the RuleTags are created with default parameters.
   * <p>
   * Note: You have to use {@link RuleTag#create(String, Class, String)} to instantiate the 'ruleTags' parameter of this method.
   * </p>
   *
   * @param ruleTag The {@link RuleTag} configurations for the 'create'.
   * @return A {@link ConfigurationReport} containing all details of the RuleTag configuration,
   * including if it was successful or not.
   * @see RuleTagConfigurationManager#createRule(RuleTag)
   */
  ConfigurationReport createRules(List<RuleTag> ruleTags);


}
