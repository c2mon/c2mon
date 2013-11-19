/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.shared.daq.datatag;


import org.apache.log4j.or.ObjectRenderer;


/**
 * Log4j ObjectRenderer for the SourceDataTagValue class.
 *
 * The purpose of an object render is to log objects of a certain class
 * in a uniform way. 
 *
 * <PRE>
 * Logger log = Logger.getLogger( ... );
 * SourceDataTagValue tag =  ... ;
 * log.info(tag);
 * ...
 * tag.log();
 * </PRE>
 *
 * The output in the log file will be (for example) : 
 * <PRE>
 * 651	nzmey.test33.xs11	2004-02-23 13:14:19.505	2004-02-23 13:13:01.505 null	false	Aliv of Process Driver.TDSexpired
 * </PRE>
 */
 
public class SourceDataTagValueRenderer implements ObjectRenderer {

  /**
   * Default constructor.
   */
  public SourceDataTagValueRenderer() {/* Nothing to do */}

  /**
   * Implementation of the ObjectRenderer interface
   * @param o   the DataTagCacheObject to be rendered
   * @return    a string representation of the DataTagCacheObject, null if the
   * object passed as a parameter is null.
   */
  public String doRender(Object o) {
    if (o != null) {
      if (o instanceof SourceDataTagValue) {
        SourceDataTagValue tag = (SourceDataTagValue) o;
        StringBuffer str = new StringBuffer();

        str.append(tag.getId());
        str.append('\t');
        str.append(tag.getName());
        str.append('\t');
        str.append(tag.getTimestamp());
        str.append('\t');
        str.append(tag.getTimestamp().getTime());
        str.append('\t');
        str.append(tag.getValue());
        str.append('\t');
        str.append(tag.getDataType());
        if (tag.getQuality() != null && ! tag.getQuality().isValid()) {
          str.append('\t');
          str.append(tag.getQuality().getQualityCode());
          str.append('\t');
          str.append(tag.getQuality().getDescription());
        }
        else {
          str.append("\t0\tOK");
        }
        if (tag.getValueDescription() != null) {
          str.append('\t');
          // remove all \n and replace all \t characters of the value description string
          str.append(tag.getValueDescription().replace("\n", "").replace("\t", "  ") );
        }
        return str.toString(); 
      } else {
        // if some jerk passed an object other than SourceDataTagCacheObject
        return o.toString();
      }
    } else {
      // if somebody decided to pass a null parameter
      return null;      
    }
  }
}
