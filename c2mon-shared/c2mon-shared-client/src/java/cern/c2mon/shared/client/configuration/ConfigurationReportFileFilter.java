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