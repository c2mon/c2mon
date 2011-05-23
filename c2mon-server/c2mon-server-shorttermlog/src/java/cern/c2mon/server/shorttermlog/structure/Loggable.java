/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
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
package cern.c2mon.server.shorttermlog.structure;

/**
 * Should be implemented by any object that needs logging to the database
 * STL account and wishing to use the default logging implementation. The
 * methods are only used for debug log messages.
 * 
 * @author Mark Brightwell
 *
 */
public interface Loggable {

  /**
   * Returns the id of the object to be logged.
   * @return the id as String
   */
  String getId();
  
  /**
   * Returns the value of the object to be logged
   * @return the value as String
   */
  String getValue();
  
}
