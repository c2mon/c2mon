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

/**
 * Describes methods that can be used for handling {@link HistoryTag}s.
 * 
 * @author vdeila
 */
public interface HistoryTagManager {

  /**
   * Creates a history tag based on the <code>expression</code>. Note that
   * {@link #subscribeToConfiguration(HistoryTagConfiguration, HistoryTagManagerListener)}
   * should be called after creating it. (Else it won't do much)
   * 
   * @param expression
   *          an expression created with the
   *          {@link HistoryTagConfiguration#createExpression()}
   * @return a history tag with the given <code>expression</code> as the
   *         configuration.
   */
  HistoryTag createHistoryTag(String expression);

  /**
   * Creates a history tag based on the <code>configuration</code>. Note that
   * {@link #subscribeToConfiguration(HistoryTagConfiguration, HistoryTagManagerListener)}
   * should be called after creating it. (Else it won't do much)
   * 
   * @param configuration
   *          the configuration to use for the history tag
   * @return a history tag with the given configuration
   */
  HistoryTag createHistoryTag(HistoryTagConfiguration configuration);

  /**
   * @return an empty configuration with the default values
   */
  HistoryTagConfiguration createEmptyConfiguration();

  /**
   * @param expression
   *          the expression to initiate the configuration with
   * @return a HistoryTagConfiguration with the given <code>expression</code> as
   *         the initial configuration
   * @throws HistoryTagExpressionException
   *           if any of the arguments in the expression is invalid
   */
  HistoryTagConfiguration createConfiguration(String expression) throws HistoryTagExpressionException;

  /**
   * Subscribes the <code>listener</code> for the given
   * <code>configuration</code>. When the historical data for the given
   * <code>configuration</code> are retrieved it will call the listener.
   * 
   * The
   * 
   * @param configuration
   *          the configuration to subscribe to
   * @param listener
   *          the listener that subscribes to the data
   */
  void subscribeToConfiguration(HistoryTagConfiguration configuration, HistoryTagManagerListener listener);

  /**
   * @param configuration
   *          the configuration to subscribe to
   * @param listener
   *          the listener that subscribes to the data
   */
  void unsubscribeFromConfiguration(HistoryTagConfiguration configuration, HistoryTagManagerListener listener);

}
