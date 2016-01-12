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

package cern.c2mon.daq.jec.plc;

/**
This Exception should be used to inform the driver about errors that  
occures while specialized subclass of EquipmentMessageHandler connects
to or disconnecting from equipment.
 */
public class JECIndexOutOfRangeException extends Exception
{
  /**
     * Serial Version UID for the JECIndexOutOfRangeException class
     */
    private static final long serialVersionUID = -5047353888919481015L;

  /**
   * The constructor
   */
  public JECIndexOutOfRangeException(String descr)
  {
    super(descr);
  }
}
