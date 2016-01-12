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
package cern.c2mon.client.ext.history.alarm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * This service allows querying {@link Alarm} history from the c2mon history database.
 *
 * @author Justin Lewis Salmon
 */
public interface AlarmHistoryService {

  /**
   * Returns all results matching the given {@link HistoricAlarmQuery}.
   *
   * @param query the {@link HistoricAlarmQuery} to match
   *
   * @return the matched results
   */
  List<Alarm> findBy(HistoricAlarmQuery query);

  /**
   * Returns a {@link Page} of entities meeting the paging restriction provided in the {@code PageRequest} object and matching the given {@link
   * HistoricAlarmQuery}.
   * *
   * <p>
   * Note: pages are 0-based, i.e. asking for the 0th page will get you the first page.
   * </p>
   *
   * @param query the {@link HistoricAlarmQuery} to match
   * @param page  the paging restriction specifier
   *
   * @return a page of matched results
   */
  Page<Alarm> findBy(HistoricAlarmQuery query, PageRequest page);
}
