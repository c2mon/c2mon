/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
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
 *****************************************************************************/
package cern.c2mon.client.common.history.tag;

import java.util.Collection;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.tim.shared.common.datatag.TagQualityStatus;

/**
 * This interface describes the methods for a history tag. A history tag should
 * take an expression, and get history data based on it.
 * 
 * @see HistoryTagManager#subscribe(HistoryTag)
 * @see C2monTagManager#subscribeDataTags(java.util.Set, DataTagUpdateListener)
 * 
 * @author vdeila
 */
public interface HistoryTag extends ClientDataTagValue, DataTagUpdateListener, HistoryTagManagerListener {

  /** The quality status for the history data is loading */
  TagQualityStatus QUALITY_STATUS_LOADING = TagQualityStatus.UNINITIALISED;
  
  /** The quality status if the expression couldn't be interpreted */
  TagQualityStatus QUALITY_STATUS_EXPRESSION_ERROR = TagQualityStatus.UNDEFINED_TAG;
  
  /** The quality status if the loading of the history fails */
  TagQualityStatus QUALITY_STATUS_FAILED = TagQualityStatus.INACCESSIBLE;
  
  /**
   * @return the tag ids that is used
   */
  Collection<Long> getTagIds();

  /**
   * @return the configuration for the history data
   */
  HistoryTagConfiguration getConfiguration();

  /**
   * @return the expression for this tag.
   */
  String getExpression();
  
  /** 
   * @param listener the listener to add
   */
  void addDataTagUpdateListener(DataTagUpdateListener listener);
  
  /**
   * @param listener the listener to remove
   */
  void removeDataTagUpdateListener(DataTagUpdateListener listener);

}
