package ch.cern.tim.driver.jintegraInterface;

import com.linar.jintegra.*;

/**
 * Event Class 'DIOPCGroupsEventGlobalDataChangeEvent'. Generated 10/5/2006 11:24:10 AM
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
public class DIOPCGroupsEventGlobalDataChangeEvent extends java.util.EventObject {
  public DIOPCGroupsEventGlobalDataChangeEvent(Object source) { super(source); }
  public void init(int transactionID, int groupHandle, int numItems, int[] clientHandles, Object[] itemValues, int[] qualities, java.util.Date[] timeStamps) {
    this.transactionID = transactionID;
    this.groupHandle = groupHandle;
    this.numItems = numItems;
    this.clientHandles = clientHandles;
    this.itemValues = itemValues;
    this.qualities = qualities;
    this.timeStamps = timeStamps;
  }

  int transactionID;
  public final int getTransactionID() { return transactionID; }
  int groupHandle;
  public final int getGroupHandle() { return groupHandle; }
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