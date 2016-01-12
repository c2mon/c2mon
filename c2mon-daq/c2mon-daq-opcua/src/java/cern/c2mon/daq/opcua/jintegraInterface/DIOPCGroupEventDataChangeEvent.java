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
package cern.c2mon.daq.opcua.jintegraInterface;

import com.linar.jintegra.*;

/**
 * Event Class 'DIOPCGroupEventDataChangeEvent'. Generated 10/5/2006 11:24:10 AM
 * from 'G:\Users\t\timoper\Public\opcdaauto.dll<P>'
 * Generated using com2java Version 2.6 Copyright (c) Intrinsyc Software International, Inc.
 * See  <A HREF="http://j-integra.intrinsyc.com/">http://j-integra.intrinsyc.com/</A><P>
 *
 * Generator Options:
 *   AwtForOcxs = False
 *   PromptForTypeLibraries = False
 *   RetryOnReject = False
 *   IDispatchOnly = False
 *   GenBeanInfo = False
 *   LowerCaseMemberNames = True
 *   TreatInStarAsIn = False
 *   ArraysAsObjects = False
 *   OmitRestrictedMethods = False
 *   ClashPrefix = zz_
 *   ImplementConflictingInterfaces = False
 *   DontRenameSameMethods = False
 *   RenameConflictingInterfaceMethods = False
 *   ReuseMethods = False
 *
 * Command Line Only Options:
 *   MakeClsidsPublic = False
 *   DontOverwrite = False
 */
public class DIOPCGroupEventDataChangeEvent extends java.util.EventObject {
  public DIOPCGroupEventDataChangeEvent(Object source) { super(source); }
  public void init(int transactionID, int numItems, int[] clientHandles, Object[] itemValues, int[] qualities, java.util.Date[] timeStamps) {
    this.transactionID = transactionID;
    this.numItems = numItems;
    this.clientHandles = clientHandles;
    this.itemValues = itemValues;
    this.qualities = qualities;
    this.timeStamps = timeStamps;
  }

  int transactionID;
  public final int getTransactionID() { return transactionID; }
  int numItems;
  public final int getNumItems() { return numItems; }
  int[] clientHandles;
  public final int[] getClientHandles() { return clientHandles; }
  Object[] itemValues;
  public final Object[] getItemValues() { return itemValues; }
  int[] qualities;
  public final int[] getQualities() { return qualities; }
  java.util.Date[] timeStamps;
  public final java.util.Date[] getTimeStamps() { return timeStamps; }
}
