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
