package cern.c2mon.daq.opcua.jintegraInterface;

import com.linar.jintegra.*;
/**
 * Proxy for COM Interface 'IOPCActivator'. Generated 10/5/2006 11:24:10 AM
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
public class IOPCActivatorProxy extends com.linar.jintegra.Dispatch implements cern.c2mon.daq.opcua.jintegraInterface.IOPCActivator, java.io.Serializable {

  protected String getJintegraVersion() { return "2.6"; }

  static { cern.c2mon.daq.opcua.jintegraInterface.JIntegraInit.init(); }

  public static final Class targetClass = IOPCActivator.class;

  public IOPCActivatorProxy(String CLSID, String host, AuthInfo authInfo) throws java.net.UnknownHostException, java.io.IOException{
    super(CLSID, IOPCActivator.IID, host, authInfo);
  }

  /** For internal use only */
  public IOPCActivatorProxy() {}

  public IOPCActivatorProxy(Object obj) throws java.io.IOException {
    super(obj, IOPCActivator.IID);
  }

  protected IOPCActivatorProxy(Object obj, String iid) throws java.io.IOException {
    super(obj, iid);
  }

  protected IOPCActivatorProxy(String CLSID, String iid, String host, AuthInfo authInfo) throws java.io.IOException {
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
              Object nodeName) throws java.io.IOException, com.linar.jintegra.AutomationException{
    cern.c2mon.daq.opcua.jintegraInterface.IOPCAutoServer zz_retVal[] = { null };
    Object zz_parameters[] = { server, progID, nodeName == null ? new Variant("nodeName", Variant.VT_ERROR, Variant.DISP_E_PARAMNOTFOUND) : nodeName, zz_retVal };
    vtblInvoke("attach", 7, zz_parameters);
    return (cern.c2mon.daq.opcua.jintegraInterface.IOPCAutoServer)zz_retVal[0];
  }

  /** Dummy reference from interface to proxy to make sure proxy gets compiled */
  static final int xxDummy = 0;

  static {
    com.linar.jintegra.InterfaceDesc.add("860a4800-46a4-478b-a776-7f3a019369e3", cern.c2mon.daq.opcua.jintegraInterface.IOPCActivatorProxy.class, null, 7, new com.linar.jintegra.MemberDesc[] {
        new com.linar.jintegra.MemberDesc("attach",
            new Class[] { Object.class, String.class, Object.class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("server", 13, 2, 8, null, null), 
              new com.linar.jintegra.Param("progID", 8, 2, 8, null, null), 
              new com.linar.jintegra.Param("nodeName", 12, 10, 8, null, null), 
              new com.linar.jintegra.Param("ppWrapper", 29, 20, 4, cern.c2mon.daq.opcua.jintegraInterface.IOPCAutoServer.IID, cern.c2mon.daq.opcua.jintegraInterface.IOPCAutoServerProxy.class) }),
});  }
}
