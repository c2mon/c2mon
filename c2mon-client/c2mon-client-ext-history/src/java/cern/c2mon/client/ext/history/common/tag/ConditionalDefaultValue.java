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
package cern.c2mon.client.ext.history.common.tag;


/**
 * This interface describes the method used to get a value based on other values
 * in the {@link HistoryTagConfiguration}
 * 
 * @author vdeila
 * 
 * @param <T> the type of return value
 */
interface ConditionalDefaultValue<T> {

  /**
   * 
   * @param configuration the configuration
   * @return the default default value for the given configuration
   */
  T getDefault(HistoryTagConfiguration configuration);
}
