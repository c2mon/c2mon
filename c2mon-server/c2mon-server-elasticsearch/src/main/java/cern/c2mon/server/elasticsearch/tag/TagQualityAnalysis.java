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
package cern.c2mon.server.elasticsearch.tag;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a detailed quality information provider for an {@link EsTag}.
 */
@Slf4j
@Data
public final class TagQualityAnalysis {
  /**
   * Represents the constant value for e successful
   * tag quality analysis (no invalid quality statuses)
   */
  public transient static final String OK = "OK";

  /**
   * A numeric value that indicates the status of the quality analysis
   */
  private int status;

  /**
   * Indicates whether the quality analysis is ok (good state) or not
   */
  private boolean valid;

  /**
   * A collection of the invalid statuses that may derive during the quality analysis.
   * If the quality analysis is positive, the status info, should contain only one (1) entry.
   * Otherwise, if the collection will contain all the invalid status information,
   * as separate entries.
   */
  private Collection<String> statusInfo = new ArrayList<>();

}
