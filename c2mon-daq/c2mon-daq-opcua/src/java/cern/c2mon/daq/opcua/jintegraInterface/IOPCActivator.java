package cern.c2mon.daq.opcua.jintegraInterface;

/**
 * COM Interface 'IOPCActivator'. Generated 10/5/2006 11:24:10 AM
 * from 'G:\Users\t\timoper\Public\opcdaauto.dll'<P>
 * Generated using com2java Version 2.6 Copyright (c) Intrinsyc Software International, Inc.
 * See  <A HREF="http://j-integra.intrinsyc.com/">http://j-integra.intrinsyc.com/</A><P>
 * Description: '<B>Used to associate existing COM servers with a OPCAutoServer object.</B>'
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
public interface IOPCActivator extends java.io.Serializable {
  /**
   * attach. Returns an automation wrapper instance for an server existing COM server.
   *
   * @param     server A reference to another Object (IUnknown) (in)
   * @param     progID The progID (in)
   * @param     nodeName A Variant (in, optional, pass null if not required)
   * @return    An reference to a cern.c2mon.daq.opcua.jintegraInterface.IOPCAutoServer
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public cern.c2mon.daq.opcua.jintegraInterface.IOPCAutoServer attach  (
              Object server,
              String progID,
              Object nodeName) throws java.io.IOException, com.linar.jintegra.AutomationException;


  // Constants to help J-Integra for COM dynamically map DCOM invocations to
  // interface members.  Don't worry, you will never need to explicitly use these constants.
  int IID860a4800_46a4_478b_a776_7f3a019369e3 = 1;
  /** Dummy reference to interface proxy to make sure it gets compiled */
  int xxDummy = IOPCActivatorProxy.xxDummy;
  /** Used internally by J-Integra for COM, please ignore */
  String IID = "860a4800-46a4-478b-a776-7f3a019369e3";
  String DISPID_1610743808_NAME = "attach";
}
