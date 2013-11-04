package cern.c2mon.daq.opcua.jintegraInterface;

import com.linar.jintegra.*;
/**
 * Proxy for COM Interface 'OPCItem'. Generated 10/5/2006 11:24:10 AM
 * from 'G:\Users\t\timoper\Public\opcdaauto.dll'<P>
 * Generated using com2java Version 2.6 Copyright (c) Intrinsyc Software International, Inc.
 * See  <A HREF="http://j-integra.intrinsyc.com/">http://j-integra.intrinsyc.com/</A><P>
 * Description: '<B>OPC Item object</B>'
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
public class OPCItemProxy extends com.linar.jintegra.Dispatch implements cern.c2mon.daq.opcua.jintegraInterface.OPCItem, java.io.Serializable {

  protected String getJintegraVersion() { return "2.6"; }

  static { cern.c2mon.daq.opcua.jintegraInterface.JIntegraInit.init(); }

  public static final Class targetClass = OPCItem.class;

  public OPCItemProxy(String CLSID, String host, AuthInfo authInfo) throws java.net.UnknownHostException, java.io.IOException{
    super(CLSID, OPCItem.IID, host, authInfo);
  }

  /** For internal use only */
  public OPCItemProxy() {}

  public OPCItemProxy(Object obj) throws java.io.IOException {
    super(obj, OPCItem.IID);
  }

  protected OPCItemProxy(Object obj, String iid) throws java.io.IOException {
    super(obj, iid);
  }

  protected OPCItemProxy(String CLSID, String iid, String host, AuthInfo authInfo) throws java.io.IOException {
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
   * getParent. Returns the parent OPCGroup.
   *
   * @return    An reference to a cern.c2mon.daq.opcua.jintegraInterface.OPCGroup
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public cern.c2mon.daq.opcua.jintegraInterface.OPCGroup getParent  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    cern.c2mon.daq.opcua.jintegraInterface.OPCGroup zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getParent", 7, zz_parameters);
    return (cern.c2mon.daq.opcua.jintegraInterface.OPCGroup)zz_retVal[0];
  }

  /**
   * getClientHandle. Gets the item client handle.
   *
   * @return    The clientHandle
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getClientHandle  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    int zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getClientHandle", 8, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * setClientHandle. Gets the item client handle.
   *
   * @param     clientHandle The clientHandle (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setClientHandle  (
              int clientHandle) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(clientHandle), zz_retVal };
    vtblInvoke("setClientHandle", 9, zz_parameters);
    return;
  }

  /**
   * getServerHandle. Gets the item server handle.
   *
   * @return    The serverHandle
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getServerHandle  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    int zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getServerHandle", 10, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * getAccessPath. Gets the item access path.
   *
   * @return    The accessPath
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getAccessPath  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    String zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getAccessPath", 11, zz_parameters);
    return (String)zz_retVal[0];
  }

  /**
   * getAccessRights. Gets the item access rights.
   *
   * @return    The accessRights
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getAccessRights  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    int zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getAccessRights", 12, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * getItemID. Gets the item id.
   *
   * @return    The itemID
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getItemID  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    String zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getItemID", 13, zz_parameters);
    return (String)zz_retVal[0];
  }

  /**
   * isActive. Gets the item active state.
   *
   * @return    The isActive
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public boolean isActive  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    boolean zz_retVal[] = { false };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("isActive", 14, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * setIsActive. Gets the item active state.
   *
   * @param     isActive The isActive (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setIsActive  (
              boolean isActive) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Boolean(isActive), zz_retVal };
    vtblInvoke("setIsActive", 15, zz_parameters);
    return;
  }

  /**
   * getRequestedDataType. Gets the item requested data type.
   *
   * @return    The requestedDataType
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public short getRequestedDataType  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    short zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getRequestedDataType", 16, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * setRequestedDataType. Gets the item requested data type.
   *
   * @param     requestedDataType The requestedDataType (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setRequestedDataType  (
              short requestedDataType) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Short(requestedDataType), zz_retVal };
    vtblInvoke("setRequestedDataType", 17, zz_parameters);
    return;
  }

  /**
   * getValue. Gets the current item value.
   *
   * @return    A Variant
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public Object getValue  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getValue", 18, zz_parameters);
    return (Object)zz_retVal[0];
  }

  /**
   * getQuality. Gets the current item value quality.
   *
   * @return    The quality
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getQuality  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    int zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getQuality", 19, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * getTimeStamp. Gets the current item value timestamp.
   *
   * @return    The timeStamp
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public java.util.Date getTimeStamp  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    java.util.Date zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getTimeStamp", 20, zz_parameters);
    return (java.util.Date)zz_retVal[0];
  }

  /**
   * getCanonicalDataType. Gets the item canonical data type.
   *
   * @return    The canonicalDataType
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public short getCanonicalDataType  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    short zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getCanonicalDataType", 21, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * getEUType. Gets the item EU type.
   *
   * @return    The eUType
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public short getEUType  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    short zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getEUType", 22, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * getEUInfo. Gets the item EU info.
   *
   * @return    A Variant
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public Object getEUInfo  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getEUInfo", 23, zz_parameters);
    return (Object)zz_retVal[0];
  }

  /**
   * read. 
   *
   * @param     source The source (in)
   * @param     value A Variant (out: use single element array, optional, pass single element of null if not required)
   * @param     quality A Variant (out: use single element array, optional, pass single element of null if not required)
   * @param     timeStamp A Variant (out: use single element array, optional, pass single element of null if not required)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void read  (
              short source,
              Object[] value,
              Object[] quality,
              Object[] timeStamp) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Short(source), value, quality, timeStamp, zz_retVal };
    vtblInvoke("read", 24, zz_parameters);
    return;
  }

  /**
   * write. 
   *
   * @param     value A Variant (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void write  (
              Object value) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { value == null ? new Variant("value") : value, zz_retVal };
    vtblInvoke("write", 25, zz_parameters);
    return;
  }

  /** Dummy reference from interface to proxy to make sure proxy gets compiled */
  static final int xxDummy = 0;

  static {
    com.linar.jintegra.InterfaceDesc.add("28e68f99-8d75-11d1-8dc3-3c302a000000", cern.c2mon.daq.opcua.jintegraInterface.OPCItemProxy.class, null, 7, new com.linar.jintegra.MemberDesc[] {
        new com.linar.jintegra.MemberDesc("getParent",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("parent", 29, 20, 5, cern.c2mon.daq.opcua.jintegraInterface.OPCGroup.IID, cern.c2mon.daq.opcua.jintegraInterface.OPCGroup.class) }),
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
        new com.linar.jintegra.MemberDesc("getAccessPath",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("accessPath", 8, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getAccessRights",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("accessRights", 3, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getItemID",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("itemID", 8, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("isActive",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("isActive", 11, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setIsActive",
            new Class[] { Boolean.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("isActive", 11, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getRequestedDataType",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("requestedDataType", 2, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setRequestedDataType",
            new Class[] { Short.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("requestedDataType", 2, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getValue",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("currentValue", 12, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getQuality",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("quality", 3, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getTimeStamp",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("timeStamp", 7, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getCanonicalDataType",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("canonicalDataType", 2, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getEUType",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("eUType", 2, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getEUInfo",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("eUInfo", 12, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("read",
            new Class[] { Short.TYPE, Object[].class, Object[].class, Object[].class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("source", 2, 2, 8, null, null), 
              new com.linar.jintegra.Param("value", 16396, 12, 8, null, null), 
              new com.linar.jintegra.Param("quality", 16396, 12, 8, null, null), 
              new com.linar.jintegra.Param("timeStamp", 16396, 12, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("write",
            new Class[] { Object.class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("value", 12, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
});  }
}
