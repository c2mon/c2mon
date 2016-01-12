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
package cern.c2mon.server.eslog.logger;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Tests that the settings are well defined by default to 10 shards and 0 replica.
 * @author Alban Marguet.
 */
@Slf4j
public class IndexSettingsTest {
  @Test
  public void shouldHaveTheRightMonthlyValues() {
    assertThat(IndexSettings.INDEX_MONTH_SETTINGS.getReplica(), is(0));
    assertThat(IndexSettings.INDEX_MONTH_SETTINGS.getShards(), is(10));
  }

  @Test
  public void shouldHaveTheRightDailyValues() {
    assertThat(IndexSettings.INDEX_DAILY_SETTINGS.getReplica(), is(0));
    assertThat(IndexSettings.INDEX_DAILY_SETTINGS.getShards(), is(1));
  }
}
