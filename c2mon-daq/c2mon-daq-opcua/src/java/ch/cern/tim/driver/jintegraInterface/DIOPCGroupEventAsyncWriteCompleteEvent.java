package ch.cern.tim.driver.jintegraInterface;

import com.linar.jintegra.*;

/**
 * Event Class 'DIOPCGroupEventAsyncWriteCompleteEvent'. Generated 10/5/2006 11:24:10 AM
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
public class DIOPCGroupEventAsyncWriteCompleteEvent extends java.util.EventObject {
  public DIOPCGroupEventAsyncWriteCompleteEvent(Object source) { super(source); }
  public void init(int transactionID, int numItems, int[] clientHandles, int[] errors) {
    this.transactionID = transactionID;
    this.numItems = numItems;
    this.clientHandles = clientHandles;
    this.errors = errors;
  }

  int transactionID;
  public final int getTransactionID() { return transactionID; }
  int numItems;
  public final int getNumItems() { return numItems; }
  int[] clientHandles;
  public final int[] getClientHandles() { return clientHandles; }
  int[] errors;
  public final int[] getErrors() { return errors; }
}