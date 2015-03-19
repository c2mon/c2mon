/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2015 CERN. This program is free software; you can
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
package cern.c2mon.web.configviewer.statistics.daqlog;

import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import cern.c2mon.web.configviewer.statistics.daqlog.values.BarChartValue;
import cern.c2mon.web.configviewer.statistics.daqlog.values.IChartCollectionValue;
import cern.c2mon.web.configviewer.statistics.daqlog.values.PieChartValue;
import cern.c2mon.web.configviewer.statistics.daqlog.values.StackedBarChartValue;

/**
 * This interface replaces the old iBatis mapper
 * (cern.c2mon.web.configviewer.statistics.SqlMapper) with a newer MyBatis
 * version.
 *
 * @author Justin Lewis Salmon
 */
public interface DAQLogStatisticsMapper {

  /**
   * Method returning a List of BarChartValue's read from the database.
   *
   * @param tableName the table/view from which the values must be read
   * @return the list of values
   * @throws SQLException error in database transaction
   */
  public List<BarChartValue> getBarChartData(@Param("tableName") final String tableName);

  public List<IChartCollectionValue> getBarChartCollectionData(@Param("tableName") final String tableName);

  /**
   * The static method used for collecting the stacked bar chart data from the
   * database.
   *
   * @param tableName the table/view from which the values are read
   * @return the list of values
   * @throws SQLException exception in database transaction
   */
  public List<StackedBarChartValue> getStackedBarChartData(@Param("tableName") final String tableName);

  /**
   * Static method returning getting the data from the database for collections
   * of stacked bar charts.
   *
   * @param tableName the table where the values are kept
   * @return a list of values
   * @throws SQLException if problem with database transaction
   */
  public List<IChartCollectionValue> getStackedBarChartCollectionData(@Param("tableName") final String tableName);

  /**
   * Method returning a List of PieChartValue's read from the database.
   *
   * @param tableName the table/view from which the values must be read
   * @return the list of values
   * @throws SQLException error in database transaction
   */
  public List<PieChartValue> getPieChartData(@Param("tableName") final String tableName);

  public List<IChartCollectionValue> getPieChartCollectionData(@Param("tableName") final String tableName);
}
