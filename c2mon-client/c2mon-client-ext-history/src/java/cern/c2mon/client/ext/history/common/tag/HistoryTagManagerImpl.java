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
package cern.c2mon.client.ext.history.common.tag;

import org.springframework.stereotype.Service;

import cern.c2mon.client.ext.history.tag.HistoryTagConfigurationImpl;
import cern.c2mon.client.ext.history.tag.HistoryTagImpl;

/**
 * Implements the methods described by {@link HistoryTagManager}
 * 
 * @author vdeila
 */
@Service
public class HistoryTagManagerImpl implements HistoryTagManager {

  @Override
  public HistoryTagConfiguration createConfiguration(final String expression) throws HistoryTagExpressionException {
    return HistoryTagConfigurationImpl.valueOf(expression);
  }

  @Override
  public HistoryTagConfiguration createEmptyConfiguration() {
    return new HistoryTagConfigurationImpl();
  }

  @Override
  public HistoryTag createHistoryTag(final String expression) {
    return new HistoryTagImpl(expression);
  }

  @Override
  public HistoryTag createHistoryTag(String expression, boolean allowNullValues) {
    return new HistoryTagImpl(expression, allowNullValues);
  }

  @Override
  public HistoryTag createHistoryTag(final HistoryTagConfiguration configuration) {
    return new HistoryTagImpl(configuration);
  }

  @Override
  public void subscribeToConfiguration(final HistoryTagConfiguration configuration, final HistoryTagManagerListener listener) {
    HistoryTagLoadingManager.getInstance().subscribe(configuration, listener);
  }

  @Override
  public void unsubscribeFromConfiguration(final HistoryTagConfiguration configuration, final HistoryTagManagerListener listener) {
    HistoryTagLoadingManager.getInstance().unsubscribe(configuration, listener);
  }

}
