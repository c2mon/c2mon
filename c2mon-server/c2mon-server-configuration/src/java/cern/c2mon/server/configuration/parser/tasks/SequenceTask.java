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
package cern.c2mon.server.configuration.parser.tasks;

import cern.c2mon.server.configuration.parser.tasks.util.TaskOrder;
import cern.c2mon.shared.client.configuration.ConfigurationElement;

/**
 * Wrapper class for all {@link ConfigurationElement}s which add a field for ordering.
 * SequenceTask provide the possibility of ordering all ConfigurationElements based on the
 * behavior of the ConfigurationElement.
 * @author Franz Ritter
 *
 */
public class SequenceTask implements Comparable<SequenceTask> {
  private TaskOrder order;
  private ConfigurationElement configurationElement;

  protected SequenceTask(ConfigurationElement configurationElement, TaskOrder order){
    this.configurationElement = configurationElement;
    this.order = order;
  }

  public int getOrder() {
    return order.getOrder();
  }

  @Override
  public int compareTo(SequenceTask o) {
    return (new Integer(o.getOrder()).compareTo(this.getOrder()) * -1);
  }

  public ConfigurationElement getConfigurationElement() {
    return configurationElement;
  }
}
