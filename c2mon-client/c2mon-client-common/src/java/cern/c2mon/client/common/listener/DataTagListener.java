/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2014 CERN. This program is free software; you can
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
package cern.c2mon.client.common.listener;

import java.util.Collection;

import cern.c2mon.client.common.tag.ClientDataTagValue;

/**
 * This interface extends the {@link DataTagUpdateListener} and can be used to
 * subscribe to tags via the C2monTagManager. Different to the {@link DataTagUpdateListener}
 * interface, the initial tag values are not passed to the {@link #onUpdate(ClientDataTagValue)}
 * method. Instead, the {@link #onInitialUpdate(Collection)} method gets called.
 * This allows a better differentiation between inital values and updates. However,
 * it is also possible to just subscribe with the {@link DataTagUpdateListener} interface,
 * in case this distinction is not needed.
 *
 * @author Matthias Braeger
 */
public interface DataTagListener extends DataTagUpdateListener {
  
  /**
   * This method is only called once before the listener gets subscribed
   * to list of tags, in order to provide the initial values.<p>
   * Please note, that this method has to return in order to not block
   * the listener subscription. Only after its return the {@link #onUpdate(ClientDataTagValue)}
   * will be called.
   * 
   * @param initialValues The initial values of the tags to which the 
   *        listener got subscribed to.
   */
  void onInitialUpdate(Collection<ClientDataTagValue> initialValues);
}
