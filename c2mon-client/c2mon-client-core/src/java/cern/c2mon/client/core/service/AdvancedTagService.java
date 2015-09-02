/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.client.core.service;

import cern.c2mon.client.core.TagService;
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
