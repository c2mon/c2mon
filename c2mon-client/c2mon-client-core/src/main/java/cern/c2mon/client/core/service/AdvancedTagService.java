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
package cern.c2mon.client.core.service;

import cern.c2mon.client.core.listener.TagSubscriptionListener;

/**
 * This interface extends the <code>C2monTagManager</code>
 * and provides additional functionalities to interact with
 * the <code>TagServiceImpl</code>. This interface is only
 * visible for other manager classes.
 *
 * @author Matthias Braeger
 */
public interface AdvancedTagService extends TagService {
  
  /**
   * Registers a <code>TagSubscriptionListener</code> to the <code>TagServiceImpl</code>. 
   * @param listener The listener to be registered
   * @throws NullPointerException In case that the parameter is <code>null</code>.
   */
  void addTagSubscriptionListener(TagSubscriptionListener listener);
  
  /**
   * Unregisters a <code>TagSubscriptionListener</code> from the <code>TagServiceImpl</code>. 
   * @param listener The listener to be unregistered
   * @throws NullPointerException In case that the parameter is <code>null</code>.
   */
  void removeTagSubscriptionListener(TagSubscriptionListener listener);
}
