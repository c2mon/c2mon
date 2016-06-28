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
package cern.c2mon.client.common.listener;

import java.util.Collection;

import cern.c2mon.client.common.tag.Tag;

/**
 * This interface extends the {@link BaseTagListener} and can be used to
 * subscribe to tags via the C2monTagManager. Different to the {@link BaseTagListener}
 * interface, the initial tag values are not passed to the {@link #onUpdate(Tag)}
 * method. Instead, the {@link #onInitialUpdate(Collection)} method gets called.
 * This allows a better differentiation between inital values and updates. However,
 * it is also possible to just subscribe with the {@link BaseTagListener} interface,
 * in case this distinction is not needed.
 *
 * @author Matthias Braeger
 */
public interface TagListener extends BaseTagListener {
  
  /**
   * This method is only called once before the listener gets subscribed
   * to list of tags, in order to provide the initial values.<p>
   * Please note, that this method has to return in order to not block
   * the listener subscription. Only after its return the {@link #onUpdate(Tag)}
   * will be called.
   * 
   * @param initialValues The initial values of the tags to which the 
   *        listener got subscribed to.
   */
  void onInitialUpdate(Collection<Tag> initialValues);
}
