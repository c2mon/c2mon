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

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.common.tag.Tag;
;

/**
 * An update event gets fired when a <code>Tag</code> 
 * changes either its value or its quality property.
 * 
 * You can register a <code>DataTagUpdateListener</code> with a 
 * <code>Tag</code> so as to be notified of these property changes.
 * @see Tag
 * @see DataTagListener
 * @author Matthias Braeger
 */
public interface BaseTagListener extends BaseListener<Tag> {

  /**
   * This method gets called when the value or quality property of a
   * <code>Tag</code> has changed. It receives then a <b>copy</b>
   * of the updated object in the C2MON client cache.<p>
   * Please note that this method will also receive initial tag values, if you
   * did not subscribe with the {@link DataTagListener} interface.
   * 
   * @param tagUpdate A copy of the <code>Tag</code> object with the 
   *                  updated properties
   */
  @Override
  void onUpdate(Tag tagUpdate);
}
