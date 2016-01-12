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
package cern.c2mon.shared.client.configuration;

import java.io.File;
import java.io.FileFilter;

/**
 * File filter to find configuration report files.
 *
 * @author Justin Lewis Salmon
 */
public class ConfigurationReportFileFilter implements FileFilter {

  private String id;

  /**
   * Constructor. Used when searching for all report files.
   */
  public ConfigurationReportFileFilter() {
    this.id = null;
  }

  /**
   * Constructor. Used when searching for report files belonging to a specific
   * configuration.
   *
   * @param id the id of the configuration
   */
  public ConfigurationReportFileFilter(String id) {
    this.id = id;
  }

  @Override
  public boolean accept(File pathname) {
    String extension = "";
    int i = pathname.getName().lastIndexOf('.');
    if (i > 0) {
      extension = pathname.getName().substring(i + 1);
    }

    // Match files containing the given id.
    if (this.id != null) {

      // Old reports are simply "report_<id>.xml
      if (pathname.getName().startsWith("report_" + this.id + ".") && extension.equals("xml")) {
        return true;
      }

      // New reports are "report_<id>_<timestamp>.xml
      else if (pathname.getName().startsWith("report_" + this.id + "_") && extension.equals("xml")) {
        return true;
      }

      else {
        return false;
      }
    }

    // Match all report files.
    else {
      return pathname.getName().startsWith("report_") && extension.equals("xml");
    }
  }
}
