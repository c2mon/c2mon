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
package cern.c2mon.client.common.tag;


import org.apache.log4j.or.ObjectRenderer;


/**
 * Log4j ObjectRenderer for the DataTagObject class.
 *
 * The purpose of an object render is to log objects of a certain class
 * in a uniform way.
 *
 * <PRE>
 * Logger log = Logger.getLogger( ... );
 * Tag tag =  ... ;
 * log.info(tag);
 * </PRE>
 *
 * The output in the log file will be (for example) :
 * <PRE>
 * 651	nzmey.test33.xs11	2004-02-23 13:14:19.505	2004-02-23 13:13:01.505 null	false	Aliv of Process Driver.TDSexpired
 * </PRE>
 *
 * @deprecated Got replaced by {@link TagRenderer}
 */
 @Deprecated
public class ClientDataTagValueRenderer implements ObjectRenderer {

  /**
   * Default constructor.
   */
  public ClientDataTagValueRenderer() {/* Nothing to do */}

  /**
   * Implementation of the ObjectRenderer interface
   * @param o   the <code>Tag</code> to be rendered
   * @return    a string representation of the <code>Tag</code>, null if the
   * object passed as a parameter is null.
   */
  public String doRender(Object o) {
    if (o != null) {
      if (o instanceof Tag) {
        Tag tag = (Tag) o;
        StringBuffer str = new StringBuffer();

        str.append(tag.getId());
        str.append('\t');
        str.append(tag.getName());
        str.append('\t');
        str.append(tag.getTimestamp());
        str.append('\t');
        str.append(tag.getValue());
        if (!tag.isValid()) {
          str.append('\t');
          str.append(tag.getDataTagQuality().toString());
          str.append('\t');
          str.append(tag.getDataTagQuality().getDescription());
        }
        return str.toString();
      } else {
        // if some jerk passed an object other than a DataTagCacheObject
        return o.toString();
      }
    } else {
      // if somebody decided to pass a null parameter
      return null;
    }
  }
}
