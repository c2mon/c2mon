package cern.c2mon.daq.opcua.jintegraInterface;

import com.linar.jintegra.*;
/**
 * Proxy for COM Interface 'DIOPCGroupEvent'. Generated 10/5/2006 11:24:10 AM
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
public class DIOPCGroupEventProxy extends com.linar.jintegra.Dispatch implements cern.c2mon.daq.opcua.jintegraInterface.DIOPCGroupEvent, java.io.Serializable {

  protected String getJintegraVersion() { return "2.6"; }

  static { cern.c2mon.daq.opcua.jintegraInterface.JIntegraInit.init(); }

  public static final Class targetClass = DIOPCGroupEvent.class;

  public DIOPCGroupEventProxy(String CLSID, String host, AuthInfo authInfo) throws java.net.UnknownHostException, java.io.IOException{
    super(CLSID, DIOPCGroupEvent.IID, host, authInfo);
  }

  /** For internal use only */
  public DIOPCGroupEventProxy() {}

  public DIOPCGroupEventProxy(Object obj) throws java.io.IOException {
    super(obj, DIOPCGroupEvent.IID);
  }

  protected DIOPCGroupEventProxy(Object obj, String iid) throws java.io.IOException {
    super(obj, iid);
  }

  protected DIOPCGroupEventProxy(String CLSID, String iid, String host, AuthInfo authInfo) throws java.io.IOException {
    super(CLSID, iid, host, authInfo);
  }

  public void addListener(String iidStr, Object theListener, Object theSource) throws java.io.IOException {
    super.addListener(iidStr, theListener, theSource);
  }

  public void removeListener(String iidStr, Object theListener) throws java.io.IOException {
    super.removeListener(iidStr, theListener);
  }

  /**
   * getPropertyByName. Get the value of a property dynamically at run-time, based on its name
   *
   * @return    The value of the property.
   * @param     name The name of the property to get.
   * @exception java.lang.NoSuchFieldException If the property does not exit.
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public Object getPropertyByName(String name) throws NoSuchFieldException, java.io.IOException, com.linar.jintegra.AutomationException {
    com.linar.jintegra.Variant parameters[] = {};
    return super.invoke(name, super.getDispatchIdOfName(name), 2, parameters).getVARIANT();
  }

  /**
   * getPropertyByName. Get the value of a property dynamically at run-time, based on its name and a parameter value
   *
   * @return    The value of the property.
   * @param     name The name of the property to get.
   * @param     rhs Parameter used when getting the property.
   * @exception java.lang.NoSuchFieldException If the property does not exit.
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public Object getPropertyByName(String name, Object rhs) throws NoSuchFieldException, java.io.IOException, com.linar.jintegra.AutomationException {
    com.linar.jintegra.Variant parameters[] = {rhs == null ? new Variant("rhs", 10, 0x80020004L) : new Variant("rhs", 12, rhs)};
    return super.invoke(name, super.getDispatchIdOfName(name), 2, parameters).getVARIANT();
  }

  /**
   * invokeMethodByName. Invoke a method dynamically at run-time
   *
   * @return    The value returned by the method (null if none).
   * @param     name The name of the method to be invoked
   * @param     parameters One element for each parameter.  Use primitive type wrappers
   *            to pass primitive types (eg Integer to pass an int).
   * @exception java.lang.NoSuchMethodException If the method does not exit.
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public Object invokeMethodByName(String name, Object[] parameters) throws NoSuchMethodException, java.io.IOException, com.linar.jintegra.AutomationException {
    com.linar.jintegra.Variant variantParameters[] = new com.linar.jintegra.Variant[parameters.length];
    for(int i = 0; i < parameters.length; i++) {
      variantParameters[i] = parameters[i] == null ? new Variant("p" + i, 10, 0x80020004L) :
	                                                   new Variant("p" + i, 12, parameters[i]);
    }
    try {
      return super.invoke(name, super.getDispatchIdOfName(name), 1, variantParameters).getVARIANT();
    } catch(NoSuchFieldException nsfe) {
      throw new NoSuchMethodException("There is no method called " + name);
    }
  }

  /**
   * invokeMethodByName. Invoke a method dynamically at run-time
   *
   * @return    The value returned by the method (null if none).
   * @param     name The name of the method to be invoked
   * @exception java.lang.NoSuchMethodException If the method does not exit.
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public Object invokeMethodByName(String name) throws NoSuchMethodException, java.io.IOException, com.linar.jintegra.AutomationException {
      return invokeMethodByName(name, new Object[]{});
  }

  /**
   * dataChange. 
   *
   * @param     theEvent The event
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void dataChange  (cern.c2mon.daq.opcua.jintegraInterface.DIOPCGroupEventDataChangeEvent theEvent) throws java.io.IOException, com.linar.jintegra.AutomationException{
    com.linar.jintegra.Variant[] parameters = {new Variant("theEvent.transactionID", 3, theEvent.transactionID),
                                         new Variant("theEvent.numItems", 3, theEvent.numItems),
                                         new Variant("theEvent.clientHandles", 8195, theEvent.clientHandles),
                                         theEvent.itemValues == null ? new Variant("theEvent.itemValues", 0, 0x80020004L) : new Variant("theEvent.itemValues", 8204, theEvent.itemValues),
                                         new Variant("theEvent.qualities", 8195, theEvent.qualities),
                                         new Variant("theEvent.timeStamps", 8199, theEvent.timeStamps),};
    invoke("dataChange", 1, 1, parameters);
    return;
  }

  /**
   * asyncReadComplete. 
   *
   * @param     theEvent The event
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void asyncReadComplete  (cern.c2mon.daq.opcua.jintegraInterface.DIOPCGroupEventAsyncReadCompleteEvent theEvent) throws java.io.IOException, com.linar.jintegra.AutomationException{
    com.linar.jintegra.Variant[] parameters = {new Variant("theEvent.transactionID", 3, theEvent.transactionID),
                                         new Variant("theEvent.numItems", 3, theEvent.numItems),
                                         new Variant("theEvent.clientHandles", 8195, theEvent.clientHandles),
                                         theEvent.itemValues == null ? new Variant("theEvent.itemValues", 0, 0x80020004L) : new Variant("theEvent.itemValues", 8204, theEvent.itemValues),
                                         new Variant("theEvent.qualities", 8195, theEvent.qualities),
                                         new Variant("theEvent.timeStamps", 8199, theEvent.timeStamps),
                                         new Variant("theEvent.errors", 8195, theEvent.errors),};
    invoke("asyncReadComplete", 2, 1, parameters);
    return;
  }

  /**
   * asyncWriteComplete. 
   *
   * @param     theEvent The event
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void asyncWriteComplete  (cern.c2mon.daq.opcua.jintegraInterface.DIOPCGroupEventAsyncWriteCompleteEvent theEvent) throws java.io.IOException, com.linar.jintegra.AutomationException{
    com.linar.jintegra.Variant[] parameters = {new Variant("theEvent.transactionID", 3, theEvent.transactionID),
                                         new Variant("theEvent.numItems", 3, theEvent.numItems),
                                         new Variant("theEvent.clientHandles", 8195, theEvent.clientHandles),
                                         new Variant("theEvent.errors", 8195, theEvent.errors),};
    invoke("asyncWriteComplete", 3, 1, parameters);
    return;
  }

  /**
   * asyncCancelComplete. 
   *
   * @param     theEvent The event
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void asyncCancelComplete  (cern.c2mon.daq.opcua.jintegraInterface.DIOPCGroupEventAsyncCancelCompleteEvent theEvent) throws java.io.IOException, com.linar.jintegra.AutomationException{
    com.linar.jintegra.Variant[] parameters = {new Variant("theEvent.cancelID", 3, theEvent.cancelID),};
    invoke("asyncCancelComplete", 4, 1, parameters);
    return;
  }

  /** Dummy reference from interface to proxy to make sure proxy gets compiled */
  static final int xxDummy = 0;

  static {
    com.linar.jintegra.InterfaceDesc.add("28e68f97-8d75-11d1-8dc3-3c302a000000", cern.c2mon.daq.opcua.jintegraInterface.DIOPCGroupEventProxy.class, null, 0, null );
  }
}
