package cern.c2mon.daq.opcua.jintegraInterface;

import java.io.IOException;

import com.linar.jintegra.*;
/**
 * Proxy for COM Interface 'IOPCGroup'. Generated 10/5/2006 11:24:10 AM
 * from 'G:\Users\t\timoper\Public\opcdaauto.dll'<P>
 * Generated using com2java Version 2.6 Copyright (c) Intrinsyc Software International, Inc.
 * See  <A HREF="http://j-integra.intrinsyc.com/">http://j-integra.intrinsyc.com/</A><P>
 * Description: '<B>OPC Group Object</B>'
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
public class IOPCGroupProxy extends com.linar.jintegra.Dispatch implements cern.c2mon.daq.opcua.jintegraInterface.IOPCGroup, java.io.Serializable {

  protected String getJintegraVersion() { return "2.6"; }

  static { cern.c2mon.daq.opcua.jintegraInterface.JIntegraInit.init(); }

  public static final Class targetClass = IOPCGroup.class;

  public IOPCGroupProxy(String CLSID, String host, AuthInfo authInfo) throws java.net.UnknownHostException, java.io.IOException{
    super(CLSID, IOPCGroup.IID, host, authInfo);
  }

  /** For internal use only */
  public IOPCGroupProxy() {}

  public IOPCGroupProxy(Object obj) throws java.io.IOException {
    super(obj, IOPCGroup.IID);
  }

  protected IOPCGroupProxy(Object obj, String iid) throws java.io.IOException {
    super(obj, iid);
  }

  protected IOPCGroupProxy(String CLSID, String iid, String host, AuthInfo authInfo) throws java.io.IOException {
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
   * getParent. Gets the parent OPCServer
   *
   * @return    An reference to a cern.c2mon.daq.opcua.jintegraInterface.IOPCAutoServer
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public cern.c2mon.daq.opcua.jintegraInterface.IOPCAutoServer getParent  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    cern.c2mon.daq.opcua.jintegraInterface.IOPCAutoServer zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getParent", 7, zz_parameters);
    return (cern.c2mon.daq.opcua.jintegraInterface.IOPCAutoServer)zz_retVal[0];
  }

  /**
   * getName. Gets the group name.
   *
   * @return    The name
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getName  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    String zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getName", 8, zz_parameters);
    return (String)zz_retVal[0];
  }

  /**
   * setName. Gets the group name.
   *
   * @param     name The name (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setName  (
              String name) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { name, zz_retVal };
    vtblInvoke("setName", 9, zz_parameters);
    return;
  }

  /**
   * isPublic. Indicates whether a group is public or private.
   *
   * @return    The isPublic
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public boolean isPublic  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    boolean zz_retVal[] = { false };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("isPublic", 10, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * isActive. Gets the group active state.
   *
   * @return    The isActive
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public boolean isActive  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    boolean zz_retVal[] = { false };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("isActive", 11, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * setIsActive. Gets the group active state.
   *
   * @param     isActive The isActive (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setIsActive  (
              boolean isActive) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Boolean(isActive), zz_retVal };
    vtblInvoke("setIsActive", 12, zz_parameters);
    return;
  }

  /**
   * isSubscribed. Gets whether asynchronous updates are enabled.
   *
   * @return    The isSubscribed
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public boolean isSubscribed  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    boolean zz_retVal[] = { false };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("isSubscribed", 13, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * setIsSubscribed. Gets whether asynchronous updates are enabled.
   *
   * @param     isSubscribed The isSubscribed (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setIsSubscribed  (
              boolean isSubscribed) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Boolean(isSubscribed), zz_retVal };
    vtblInvoke("setIsSubscribed", 14, zz_parameters);
    return;
  }

  /**
   * getClientHandle. Gets the group client handle.
   *
   * @return    The clientHandle
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getClientHandle  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    int zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getClientHandle", 15, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * setClientHandle. Gets the group client handle.
   *
   * @param     clientHandle The clientHandle (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setClientHandle  (
              int clientHandle) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(clientHandle), zz_retVal };
    vtblInvoke("setClientHandle", 16, zz_parameters);
    return;
  }

  /**
   * getServerHandle. Gets the group server handle.
   *
   * @return    The serverHandle
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getServerHandle  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    int zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getServerHandle", 17, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * getLocaleID. Gets the group locale setting.
   *
   * @return    The localeID
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getLocaleID  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    int zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getLocaleID", 18, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * setLocaleID. Gets the group locale setting.
   *
   * @param     localeID The localeID (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setLocaleID  (
              int localeID) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(localeID), zz_retVal };
    vtblInvoke("setLocaleID", 19, zz_parameters);
    return;
  }

  /**
   * getTimeBias. Gets the group time bias.
   *
   * @return    The timeBias
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getTimeBias  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    int zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getTimeBias", 20, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * setTimeBias. Gets the group time bias.
   *
   * @param     timeBias The timeBias (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setTimeBias  (
              int timeBias) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(timeBias), zz_retVal };
    vtblInvoke("setTimeBias", 21, zz_parameters);
    return;
  }

  /**
   * getDeadBand. Gets the group deadband.
   *
   * @return    The deadBand
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public float getDeadBand  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    float zz_retVal[] = { 0.0F };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getDeadBand", 22, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * setDeadBand. Gets the group deadband.
   *
   * @param     deadBand The deadBand (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setDeadBand  (
              float deadBand) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Float(deadBand), zz_retVal };
    vtblInvoke("setDeadBand", 23, zz_parameters);
    return;
  }

  /**
   * getUpdateRate. Gets the group update rate in milliseconds.
   *
   * @return    The updateRate
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getUpdateRate  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    int zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getUpdateRate", 24, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * setUpdateRate. Gets the group update rate in milliseconds.
   *
   * @param     updateRate The updateRate (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setUpdateRate  (
              int updateRate) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(updateRate), zz_retVal };
    vtblInvoke("setUpdateRate", 25, zz_parameters);
    return;
  }

  /**
   * getOPCItems. Returns the OPCItems collection
   *
   * @return    An reference to a cern.c2mon.daq.opcua.jintegraInterface.OPCItems
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public cern.c2mon.daq.opcua.jintegraInterface.OPCItems getOPCItems  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    cern.c2mon.daq.opcua.jintegraInterface.OPCItems zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getOPCItems", 26, zz_parameters);
    return (cern.c2mon.daq.opcua.jintegraInterface.OPCItems)zz_retVal[0];
  }

  /**
   * syncRead. 
   *
   * @param     source The source (in)
   * @param     numItems The numItems (in)
   * @param     serverHandles The serverHandles (in)
   * @param     values A Variant (out: use single element array)
   * @param     errors The errors (out: use single element array)
   * @param     qualities A Variant (out: use single element array, optional, pass single element of null if not required)
   * @param     timeStamps A Variant (out: use single element array, optional, pass single element of null if not required)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void syncRead  (
              short source,
              int numItems,
              int[] serverHandles,
              Object[][] values,
              int[][] errors,
              Object[] qualities,
              Object[] timeStamps) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Short(source), new Integer(numItems), serverHandles, values, errors, qualities, timeStamps, zz_retVal };
    vtblInvoke("syncRead", 27, zz_parameters);
    return;
  }

  /**
   * syncWrite. 
   *
   * @param     numItems The numItems (in)
   * @param     serverHandles The serverHandles (in)
   * @param     values A Variant (in)
   * @param     errors The errors (out: use single element array)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void syncWrite  (
              int numItems,
              int[] serverHandles,
              Object[] values,
              int[][] errors) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(numItems), serverHandles, values, errors, zz_retVal };
    vtblInvoke("syncWrite", 28, zz_parameters);
    return;
  }

  /**
   * asyncRead. 
   *
   * @param     numItems The numItems (in)
   * @param     serverHandles The serverHandles (in)
   * @param     errors The errors (out: use single element array)
   * @param     transactionID The transactionID (in)
   * @param     cancelID The cancelID (out: use single element array)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void asyncRead  (
              int numItems,
              int[] serverHandles,
              int[][] errors,
              int transactionID,
              int[] cancelID) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(numItems), serverHandles, errors, new Integer(transactionID), cancelID, zz_retVal };
    vtblInvoke("asyncRead", 29, zz_parameters);
    return;
  }

  /**
   * asyncWrite. 
   *
   * @param     numItems The numItems (in)
   * @param     serverHandles The serverHandles (in)
   * @param     values A Variant (in)
   * @param     errors The errors (out: use single element array)
   * @param     transactionID The transactionID (in)
   * @param     cancelID The cancelID (out: use single element array)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void asyncWrite  (
              int numItems,
              int[] serverHandles,
              Object[] values,
              int[][] errors,
              int transactionID,
              int[] cancelID) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(numItems), serverHandles, values, errors, new Integer(transactionID), cancelID, zz_retVal };
    vtblInvoke("asyncWrite", 30, zz_parameters);
    return;
  }

  /**
   * asyncRefresh. 
   *
   * @param     source The source (in)
   * @param     transactionID The transactionID (in)
   * @param     cancelID The cancelID (out: use single element array)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void asyncRefresh  (
              short source,
              int transactionID,
              int[] cancelID) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Short(source), new Integer(transactionID), cancelID, zz_retVal };
    vtblInvoke("asyncRefresh", 31, zz_parameters);
    return;
  }

  /**
   * asyncCancel. 
   *
   * @param     cancelID The cancelID (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void asyncCancel  (
              int cancelID) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(cancelID), zz_retVal };
    vtblInvoke("asyncCancel", 32, zz_parameters);
    return;
  }

  /** Dummy reference from interface to proxy to make sure proxy gets compiled */
  static final int xxDummy = 0;

  static {
    com.linar.jintegra.InterfaceDesc.add("28e68f96-8d75-11d1-8dc3-3c302a000000", cern.c2mon.daq.opcua.jintegraInterface.IOPCGroupProxy.class, null, 7, new com.linar.jintegra.MemberDesc[] {
        new com.linar.jintegra.MemberDesc("getParent",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("ppParent", 29, 20, 4, cern.c2mon.daq.opcua.jintegraInterface.IOPCAutoServer.IID, cern.c2mon.daq.opcua.jintegraInterface.IOPCAutoServerProxy.class) }),
        new com.linar.jintegra.MemberDesc("getName",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("name", 8, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setName",
            new Class[] { String.class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("name", 8, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("isPublic",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("isPublic", 11, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("isActive",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("isActive", 11, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setIsActive",
            new Class[] { Boolean.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("isActive", 11, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("isSubscribed",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("isSubscribed", 11, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setIsSubscribed",
            new Class[] { Boolean.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("isSubscribed", 11, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getClientHandle",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("clientHandle", 3, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setClientHandle",
            new Class[] { Integer.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("clientHandle", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getServerHandle",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("serverHandle", 3, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getLocaleID",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("localeID", 3, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setLocaleID",
            new Class[] { Integer.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("localeID", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getTimeBias",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("timeBias", 3, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setTimeBias",
            new Class[] { Integer.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("timeBias", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getDeadBand",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("deadBand", 4, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setDeadBand",
            new Class[] { Float.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("deadBand", 4, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getUpdateRate",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("updateRate", 3, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setUpdateRate",
            new Class[] { Integer.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("updateRate", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getOPCItems",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("ppItems", 29, 20, 4, cern.c2mon.daq.opcua.jintegraInterface.OPCItems.IID, cern.c2mon.daq.opcua.jintegraInterface.OPCItemsProxy.class) }),
        new com.linar.jintegra.MemberDesc("syncRead",
            new Class[] { Short.TYPE, Integer.TYPE, int[].class, Object[][].class, int[][].class, Object[].class, Object[].class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("source", 2, 2, 8, null, null), 
              new com.linar.jintegra.Param("numItems", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("serverHandles", 3, 67, 8, null, null), 
              new com.linar.jintegra.Param("values", 16396, 5, 8, null, null), 
              new com.linar.jintegra.Param("errors", 16387, 5, 8, null, null), 
              new com.linar.jintegra.Param("qualities", 16396, 12, 8, null, null), 
              new com.linar.jintegra.Param("timeStamps", 16396, 12, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("syncWrite",
            new Class[] { Integer.TYPE, int[].class, Object[].class, int[][].class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("numItems", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("serverHandles", 3, 67, 8, null, null), 
              new com.linar.jintegra.Param("values", 12, 67, 8, null, null), 
              new com.linar.jintegra.Param("errors", 16387, 5, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("asyncRead",
            new Class[] { Integer.TYPE, int[].class, int[][].class, Integer.TYPE, int[].class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("numItems", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("serverHandles", 3, 67, 8, null, null), 
              new com.linar.jintegra.Param("errors", 16387, 5, 8, null, null), 
              new com.linar.jintegra.Param("transactionID", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("cancelID", 16387, 4, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("asyncWrite",
            new Class[] { Integer.TYPE, int[].class, Object[].class, int[][].class, Integer.TYPE, int[].class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("numItems", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("serverHandles", 3, 67, 8, null, null), 
              new com.linar.jintegra.Param("values", 12, 67, 8, null, null), 
              new com.linar.jintegra.Param("errors", 16387, 5, 8, null, null), 
              new com.linar.jintegra.Param("transactionID", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("cancelID", 16387, 4, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("asyncRefresh",
            new Class[] { Short.TYPE, Integer.TYPE, int[].class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("source", 2, 2, 8, null, null), 
              new com.linar.jintegra.Param("transactionID", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("cancelID", 16387, 4, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("asyncCancel",
            new Class[] { Integer.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("cancelID", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
});  }

    @Override
    public void addDIOPCGroupEventListener(DIOPCGroupEvent theListener) throws IOException {
        // does nothing
    }
}
