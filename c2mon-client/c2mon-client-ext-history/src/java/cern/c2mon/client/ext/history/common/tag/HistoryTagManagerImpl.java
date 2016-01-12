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
