package cern.c2mon.daq.opcua.jintegraInterface;

import com.linar.jintegra.*;
/**
 * Proxy for COM Interface 'IOPCAutoServer'. Generated 10/5/2006 11:24:10 AM
 * from 'G:\Users\t\timoper\Public\opcdaauto.dll'<P>
 * Generated using com2java Version 2.6 Copyright (c) Intrinsyc Software International, Inc.
 * See  <A HREF="http://j-integra.intrinsyc.com/">http://j-integra.intrinsyc.com/</A><P>
 * Description: '<B>OPCServer Object</B>'
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
public class IOPCAutoServerProxy extends com.linar.jintegra.Dispatch implements cern.c2mon.daq.opcua.jintegraInterface.IOPCAutoServer, java.io.Serializable {

  protected String getJintegraVersion() { return "2.6"; }

  static { cern.c2mon.daq.opcua.jintegraInterface.JIntegraInit.init(); }

  public static final Class targetClass = IOPCAutoServer.class;

  public IOPCAutoServerProxy(String CLSID, String host, AuthInfo authInfo) throws java.net.UnknownHostException, java.io.IOException{
    super(CLSID, IOPCAutoServer.IID, host, authInfo);
  }

  /** For internal use only */
  public IOPCAutoServerProxy() {}

  public IOPCAutoServerProxy(Object obj) throws java.io.IOException {
    super(obj, IOPCAutoServer.IID);
  }

  protected IOPCAutoServerProxy(Object obj, String iid) throws java.io.IOException {
    super(obj, iid);
  }

  protected IOPCAutoServerProxy(String CLSID, String iid, String host, AuthInfo authInfo) throws java.io.IOException {
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
   * getStartTime. Gets the start time of the OPC server.
   *
   * @return    The startTime
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public java.util.Date getStartTime  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    java.util.Date zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getStartTime", 7, zz_parameters);
    return (java.util.Date)zz_retVal[0];
  }

  /**
   * getCurrentTime. Gets the current time of the OPC server.
   *
   * @return    The currentTime
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public java.util.Date getCurrentTime  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    java.util.Date zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getCurrentTime", 8, zz_parameters);
    return (java.util.Date)zz_retVal[0];
  }

  /**
   * getLastUpdateTime. Gets the last time the OPC server sent a data update.
   *
   * @return    The lastUpdateTime
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public java.util.Date getLastUpdateTime  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    java.util.Date zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getLastUpdateTime", 9, zz_parameters);
    return (java.util.Date)zz_retVal[0];
  }

  /**
   * getMajorVersion. Gets the major version number for the OPC server.
   *
   * @return    The majorVersion
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public short getMajorVersion  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    short zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getMajorVersion", 10, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * getMinorVersion. Gets the minor version number for the OPC server.
   *
   * @return    The minorVersion
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public short getMinorVersion  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    short zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getMinorVersion", 11, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * getBuildNumber. Gets the build number for the OPC server.
   *
   * @return    The buildNumber
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public short getBuildNumber  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    short zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getBuildNumber", 12, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * getVendorInfo. Gets an identifier for the OPC server's vendor.
   *
   * @return    The vendorInfo
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getVendorInfo  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    String zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getVendorInfo", 13, zz_parameters);
    return (String)zz_retVal[0];
  }

  /**
   * getServerState. Gets the current OPC server state.
   *
   * @return    The serverState
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getServerState  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    int zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getServerState", 14, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * getServerName. Gets the OPC server name.
   *
   * @return    The serverName
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getServerName  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    String zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getServerName", 15, zz_parameters);
    return (String)zz_retVal[0];
  }

  /**
   * getServerNode. Gets the OPC server node name.
   *
   * @return    The serverNode
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getServerNode  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    String zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getServerNode", 16, zz_parameters);
    return (String)zz_retVal[0];
  }

  /**
   * getClientName. Gets the current client name.
   *
   * @return    The clientName
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getClientName  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    String zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getClientName", 17, zz_parameters);
    return (String)zz_retVal[0];
  }

  /**
   * setClientName. Gets the current client name.
   *
   * @param     clientName The clientName (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setClientName  (
              String clientName) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { clientName, zz_retVal };
    vtblInvoke("setClientName", 18, zz_parameters);
    return;
  }

  /**
   * getLocaleID. Gets the current language/locale setting.
   *
   * @return    The localeID
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getLocaleID  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    int zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getLocaleID", 19, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * setLocaleID. Gets the current language/locale setting.
   *
   * @param     localeID The localeID (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setLocaleID  (
              int localeID) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(localeID), zz_retVal };
    vtblInvoke("setLocaleID", 20, zz_parameters);
    return;
  }

  /**
   * getBandwidth. Gets an estimate of current OPC server utilization as a percentage.
   *
   * @return    The bandwidth
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getBandwidth  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    int zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getBandwidth", 21, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * getOPCGroups. The collection of OPCGroup Objects
   *
   * @return    An reference to a cern.c2mon.daq.opcua.jintegraInterface.OPCGroups
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public cern.c2mon.daq.opcua.jintegraInterface.OPCGroups getOPCGroups  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    cern.c2mon.daq.opcua.jintegraInterface.OPCGroups zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getOPCGroups", 22, zz_parameters);
    return (cern.c2mon.daq.opcua.jintegraInterface.OPCGroups)zz_retVal[0];
  }

  /**
   * getPublicGroupNames. Gets a list of public group names.
   *
   * @return    A Variant
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public Object getPublicGroupNames  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getPublicGroupNames", 23, zz_parameters);
    return (Object)zz_retVal[0];
  }

  /**
   * getOPCServers. Returns an array of Server names on the specified node
   *
   * @param     node A Variant (in, optional, pass null if not required)
   * @return    A Variant
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public Object getOPCServers  (
              Object node) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { node == null ? new Variant("node", Variant.VT_ERROR, Variant.DISP_E_PARAMNOTFOUND) : node, zz_retVal };
    vtblInvoke("getOPCServers", 24, zz_parameters);
    return (Object)zz_retVal[0];
  }

  /**
   * connect. Connects to an OPC Server with the specified name and node
   *
   * @param     progID The progID (in)
   * @param     node A Variant (in, optional, pass null if not required)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void connect  (
              String progID,
              Object node) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { progID, node == null ? new Variant("node", Variant.VT_ERROR, Variant.DISP_E_PARAMNOTFOUND) : node, zz_retVal };
    vtblInvoke("connect", 25, zz_parameters);
    return;
  }

  /**
   * disconnect. Terminate the connection with the OPC Server
   *
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void disconnect  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("disconnect", 26, zz_parameters);
    return;
  }

  /**
   * createBrowser. Create a new OPCBrowser Object
   *
   * @return    An reference to a cern.c2mon.daq.opcua.jintegraInterface.OPCBrowser
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public cern.c2mon.daq.opcua.jintegraInterface.OPCBrowser createBrowser  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    cern.c2mon.daq.opcua.jintegraInterface.OPCBrowser zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("createBrowser", 27, zz_parameters);
    return (cern.c2mon.daq.opcua.jintegraInterface.OPCBrowser)zz_retVal[0];
  }

  /**
   * getErrorString. Convert an error code to a descriptive string
   *
   * @param     errorCode The errorCode (in)
   * @return    The errorString
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getErrorString  (
              int errorCode) throws java.io.IOException, com.linar.jintegra.AutomationException{
    String zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(errorCode), zz_retVal };
    vtblInvoke("getErrorString", 28, zz_parameters);
    return (String)zz_retVal[0];
  }

  /**
   * queryAvailableLocaleIDs. Returns the LocaleIDs supported by this server
   *
   * @return    A Variant
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public Object queryAvailableLocaleIDs  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("queryAvailableLocaleIDs", 29, zz_parameters);
    return (Object)zz_retVal[0];
  }

  /**
   * queryAvailableProperties. Returns the properties available for the specified item.
   *
   * @param     itemID The itemID (in)
   * @param     count The count (out: use single element array)
   * @param     propertyIDs The propertyIDs (out: use single element array)
   * @param     descriptions The descriptions (out: use single element array)
   * @param     dataTypes The dataTypes (out: use single element array)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void queryAvailableProperties  (
              String itemID,
              int[] count,
              int[][] propertyIDs,
              String[][] descriptions,
              short[][] dataTypes) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { itemID, count, propertyIDs, descriptions, dataTypes, zz_retVal };
    vtblInvoke("queryAvailableProperties", 30, zz_parameters);
    return;
  }

  /**
   * getItemProperties. Returns the properties for the specified item.
   *
   * @param     itemID The itemID (in)
   * @param     count The count (in)
   * @param     propertyIDs The propertyIDs (in)
   * @param     propertyValues A Variant (out: use single element array)
   * @param     errors The errors (out: use single element array)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void getItemProperties  (
              String itemID,
              int count,
              int[] propertyIDs,
              Object[][] propertyValues,
              int[][] errors) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { itemID, new Integer(count), propertyIDs, propertyValues, errors, zz_retVal };
    vtblInvoke("getItemProperties", 31, zz_parameters);
    return;
  }

  /**
   * lookupItemIDs. Returns the item ids for the specified properties.
   *
   * @param     itemID The itemID (in)
   * @param     count The count (in)
   * @param     propertyIDs The propertyIDs (in)
   * @param     newItemIDs The newItemIDs (out: use single element array)
   * @param     errors The errors (out: use single element array)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void lookupItemIDs  (
              String itemID,
              int count,
              int[] propertyIDs,
              String[][] newItemIDs,
              int[][] errors) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { itemID, new Integer(count), propertyIDs, newItemIDs, errors, zz_retVal };
    vtblInvoke("lookupItemIDs", 32, zz_parameters);
    return;
  }

  /** Dummy reference from interface to proxy to make sure proxy gets compiled */
  static final int xxDummy = 0;

  static {
    com.linar.jintegra.InterfaceDesc.add("28e68f92-8d75-11d1-8dc3-3c302a000000", cern.c2mon.daq.opcua.jintegraInterface.IOPCAutoServerProxy.class, null, 7, new com.linar.jintegra.MemberDesc[] {
        new com.linar.jintegra.MemberDesc("getStartTime",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("startTime", 7, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getCurrentTime",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("currentTime", 7, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getLastUpdateTime",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("lastUpdateTime", 7, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getMajorVersion",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("majorVersion", 2, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getMinorVersion",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("minorVersion", 2, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getBuildNumber",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("buildNumber", 2, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getVendorInfo",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("vendorInfo", 8, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getServerState",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("serverState", 3, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getServerName",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("serverName", 8, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getServerNode",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("serverNode", 8, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getClientName",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("clientName", 8, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setClientName",
            new Class[] { String.class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("clientName", 8, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getLocaleID",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("localeID", 3, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setLocaleID",
            new Class[] { Integer.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("localeID", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getBandwidth",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("bandwidth", 3, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getOPCGroups",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("ppGroups", 29, 20, 5, cern.c2mon.daq.opcua.jintegraInterface.OPCGroups.IID, cern.c2mon.daq.opcua.jintegraInterface.OPCGroups.class) }),
        new com.linar.jintegra.MemberDesc("getPublicGroupNames",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("publicGroups", 12, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getOPCServers",
            new Class[] { Object.class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("node", 12, 10, 8, null, null), 
              new com.linar.jintegra.Param("oPCServers", 12, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("connect",
            new Class[] { String.class, Object.class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("progID", 8, 2, 8, null, null), 
              new com.linar.jintegra.Param("node", 12, 10, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("disconnect",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("createBrowser",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("ppBrowser", 29, 20, 4, cern.c2mon.daq.opcua.jintegraInterface.OPCBrowser.IID, cern.c2mon.daq.opcua.jintegraInterface.OPCBrowserProxy.class) }),
        new com.linar.jintegra.MemberDesc("getErrorString",
            new Class[] { Integer.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("errorCode", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("errorString", 8, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("queryAvailableLocaleIDs",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("localeIDs", 12, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("queryAvailableProperties",
            new Class[] { String.class, int[].class, int[][].class, String[][].class, short[][].class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("itemID", 8, 2, 8, null, null), 
              new com.linar.jintegra.Param("count", 16387, 4, 8, null, null), 
              new com.linar.jintegra.Param("propertyIDs", 16387, 5, 8, null, null), 
              new com.linar.jintegra.Param("descriptions", 16392, 5, 8, null, null), 
              new com.linar.jintegra.Param("dataTypes", 16386, 5, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getItemProperties",
            new Class[] { String.class, Integer.TYPE, int[].class, Object[][].class, int[][].class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("itemID", 8, 2, 8, null, null), 
              new com.linar.jintegra.Param("count", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("propertyIDs", 3, 67, 8, null, null), 
              new com.linar.jintegra.Param("propertyValues", 16396, 5, 8, null, null), 
              new com.linar.jintegra.Param("errors", 16387, 5, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("lookupItemIDs",
            new Class[] { String.class, Integer.TYPE, int[].class, String[][].class, int[][].class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("itemID", 8, 2, 8, null, null), 
              new com.linar.jintegra.Param("count", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("propertyIDs", 3, 67, 8, null, null), 
              new com.linar.jintegra.Param("newItemIDs", 16392, 5, 8, null, null), 
              new com.linar.jintegra.Param("errors", 16387, 5, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
});  }

@Override
public Dispatch getJintegraDispatch() {
    // TODO Auto-generated method stub
    return null;
}

@Override
public void release() {
    // TODO Auto-generated method stub
    
}
}
