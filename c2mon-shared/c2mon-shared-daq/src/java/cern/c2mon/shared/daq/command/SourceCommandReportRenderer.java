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
package cern.c2mon.shared.daq.command;

import org.apache.log4j.or.ObjectRenderer;


/**
 * Log4j ObjectRenderer for the SourceCommandTagReport class.
 *
 * The purpose of an object render is to log objects of a certain class
 * in a uniform way.
 *
 * <PRE>
 * Logger log = LoggerFactory.getLogger( ... );
 * CommandReport rep =  ... ;
 * log.info(rep);
 * ...
 * tag.log();
 * </PRE>
 *
 * The output in the log file will be (for example) :
 * <PRE>
 *
 * </PRE>
 *
 * @deprecated since we switched to slf4j, implementation-specific features like this are not a good idea
 */

public class SourceCommandReportRenderer implements ObjectRenderer {

  /**
   * Default constructor.
   */
  public SourceCommandReportRenderer() {/* Nothing to do */}

  /**
   * Implementation of the ObjectRenderer interface
   * @param o   the DataTagCacheObject to be rendered
   * @return    a string representation of the DataTagCacheObject, null if the
   * object passed as a parameter is null.
   */
  public String doRender(Object o) {
    if (o != null) {
      if (o instanceof SourceCommandTagReport) {
        SourceCommandTagReport rep = (SourceCommandTagReport) o;
        StringBuffer str = new StringBuffer();
        str.append("REPORT");
        str.append('\t');
        str.append(rep.getId());
        str.append('\t');
        str.append(rep.getName());
        str.append('\t');
        str.append(rep.getStatus());
        str.append('\t');
        str.append(rep.getFullDescription());
        str.append('\t');
        str.append(rep.getReturnValue());
        str.append('\t');
        return str.toString();
      }
      else {
        // if someone passed an object other than CommandReport
        return o.toString();
      }
    }
    else {
      // if somebody decided to pass a null parameter
      return null;
    }
  }
}
