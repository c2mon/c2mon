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
package cern.c2mon.shared.client.tag;

import java.util.Collection;
import java.util.List;

import cern.c2mon.shared.client.request.ClientRequestResult;

public interface TagConfig extends ClientRequestResult{
  /**
   * Returns the tag identifier
   * @return the tag identifier
   */
  Long getId();

  /**
   * @return The String representation of the <code>RuleExpression</code> object
   * or <code>null</code>, if the tag does not represent a rule.
   */
  String getRuleExpression();

  /**
   * @return collection of ids of Rules using this Tag
   */
  Collection<Long> getRuleIds();

  /**
   * The topic name on which these updates are expected.
   *
   * @return the JMS topic name as String
   */
  String getTopicName();

  /**
   * @return true if the Tag is a control tag
   */
  boolean isControlTag();

  /**
   * @return min value this Tag can take
   */
  String getMinValue();

  /**
   * @return max value this Tag can take
   */
  String getMaxValue();

  /**
   * @return DIP publication topic
   */
  String getDipPublication();

  /**
   * @return JAPC publication topic
   */
  String getJapcPublication();

  /**
   * @return true if guaranteed delivery is set
   */
  boolean isGuaranteedDelivery();

  /**
   * @return JMS priority
   */
  int getPriority();

  /**
   * @return Hardware address as string
   */
  String getHardwareAddress();

  /**
   * @return timedead band value
   */
  int getTimeDeadband();

  /**
   * @return value deadband setting
   */
  float getValueDeadband();

  /**
   * @return value deadband type
   */
  short getValueDeadbandType();

  /**
   * @return ids of all Alarms associated to this Tag
   */
  Collection<Long> getAlarmIds();

  /**
   * @param processNames the processNames the names of all the processes this Tag is attached to
   *                        (single one for DataTag or multiple for Rules)
   */
  void setProcessNames(List<String> processNames);

  /**
   * @return the processNames the names of all the processes this Tag is attached to
   *                         (single one for DataTag or multiple for Rules)
   */
  List<String> getProcessNames();

  /**
   * @param logged true if the Tag is logged to the STL
   */
  void setLogged(Boolean logged);

  /**
   * @return true if this Tag is logged to the STL
   */
  boolean isLogged();
}
