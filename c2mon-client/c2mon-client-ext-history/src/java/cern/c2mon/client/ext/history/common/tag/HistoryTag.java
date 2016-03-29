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

import java.util.Collection;

import cern.c2mon.client.common.listener.BaseTagListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.TagService;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

/**
 * This interface describes the methods for a history tag. A history tag should
 * take an expression, and get history data based on it.
 * 
 * @see HistoryTagManager#subscribe(HistoryTag)
 * @see TagService#subscribe(java.util.Set, BaseTagListener)
 * 
 * @author vdeila
 */
public interface HistoryTag extends Tag, BaseTagListener, HistoryTagManagerListener {

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
  void addDataTagUpdateListener(BaseTagListener listener);
  
  /**
   * @param listener the listener to remove
   */
  void removeDataTagUpdateListener(BaseTagListener listener);

}
