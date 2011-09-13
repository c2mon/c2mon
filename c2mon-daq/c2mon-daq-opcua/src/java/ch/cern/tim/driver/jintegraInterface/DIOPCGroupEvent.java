package ch.cern.tim.driver.jintegraInterface;

/**
 * COM Interface 'DIOPCGroupEvent'. Generated 10/5/2006 11:24:10 AM
 * from 'G:\Users\t\timoper\Public\opcdaauto.dll'<P>
 * Generated using com2java Version 2.6 Copyright (c) Intrinsyc Software International, Inc.
 * See  <A HREF="http://j-integra.intrinsyc.com/">http://j-integra.intrinsyc.com/</A><P>
 * Description: '<B>OPC Group Events</B>'
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
public interface DIOPCGroupEvent extends java.util.EventListener, java.io.Serializable {
  /**
   * dataChange. 
   *
   * @param     theEvent The event
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void dataChange  (ch.cern.tim.driver.jintegraInterface.DIOPCGroupEventDataChangeEvent theEvent) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * asyncReadComplete. 
   *
   * @param     theEvent The event
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void asyncReadComplete  (ch.cern.tim.driver.jintegraInterface.DIOPCGroupEventAsyncReadCompleteEvent theEvent) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * asyncWriteComplete. 
   *
   * @param     theEvent The event
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void asyncWriteComplete  (ch.cern.tim.driver.jintegraInterface.DIOPCGroupEventAsyncWriteCompleteEvent theEvent) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * asyncCancelComplete. 
   *
   * @param     theEvent The event
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void asyncCancelComplete  (ch.cern.tim.driver.jintegraInterface.DIOPCGroupEventAsyncCancelCompleteEvent theEvent) throws java.io.IOException, com.linar.jintegra.AutomationException;


  // Constants to help J-Integra for COM dynamically map DCOM invocations to
  // interface members.  Don't worry, you will never need to explicitly use these constants.
  int IID28e68f97_8d75_11d1_8dc3_3c302a000000 = 1;
  /** Dummy reference to interface proxy to make sure it gets compiled */
  int xxDummy = DIOPCGroupEventProxy.xxDummy;
  /** Used internally by J-Integra for COM, please ignore */
  String IID = "00020400-0000-0000-C000-000000000046";
  String DISPID_1_NAME = "dataChange";
  String DISPID_2_NAME = "asyncReadComplete";
  String DISPID_3_NAME = "asyncWriteComplete";
  String DISPID_4_NAME = "asyncCancelComplete";
}
