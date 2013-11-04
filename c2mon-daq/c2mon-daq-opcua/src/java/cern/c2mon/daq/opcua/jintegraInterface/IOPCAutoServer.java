package cern.c2mon.daq.opcua.jintegraInterface;

import com.linar.jintegra.RemoteObjRef;

/**
 * COM Interface 'IOPCAutoServer'. Generated 10/5/2006 11:24:10 AM
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
public interface IOPCAutoServer extends java.io.Serializable, RemoteObjRef {
  /**
   * getStartTime. Gets the start time of the OPC server.
   *
   * @return    The startTime
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public java.util.Date getStartTime  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getCurrentTime. Gets the current time of the OPC server.
   *
   * @return    The currentTime
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public java.util.Date getCurrentTime  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getLastUpdateTime. Gets the last time the OPC server sent a data update.
   *
   * @return    The lastUpdateTime
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public java.util.Date getLastUpdateTime  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getMajorVersion. Gets the major version number for the OPC server.
   *
   * @return    The majorVersion
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public short getMajorVersion  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getMinorVersion. Gets the minor version number for the OPC server.
   *
   * @return    The minorVersion
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public short getMinorVersion  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getBuildNumber. Gets the build number for the OPC server.
   *
   * @return    The buildNumber
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public short getBuildNumber  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getVendorInfo. Gets an identifier for the OPC server's vendor.
   *
   * @return    The vendorInfo
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getVendorInfo  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getServerState. Gets the current OPC server state.
   *
   * @return    The serverState
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getServerState  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getServerName. Gets the OPC server name.
   *
   * @return    The serverName
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getServerName  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getServerNode. Gets the OPC server node name.
   *
   * @return    The serverNode
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getServerNode  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getClientName. Gets the current client name.
   *
   * @return    The clientName
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getClientName  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * setClientName. Gets the current client name.
   *
   * @param     clientName The clientName (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setClientName  (
              String clientName) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getLocaleID. Gets the current language/locale setting.
   *
   * @return    The localeID
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getLocaleID  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * setLocaleID. Gets the current language/locale setting.
   *
   * @param     localeID The localeID (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setLocaleID  (
              int localeID) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getBandwidth. Gets an estimate of current OPC server utilization as a percentage.
   *
   * @return    The bandwidth
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getBandwidth  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getOPCGroups. The collection of OPCGroup Objects
   *
   * @return    An reference to a cern.c2mon.daq.opcua.jintegraInterface.OPCGroups
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public cern.c2mon.daq.opcua.jintegraInterface.OPCGroups getOPCGroups  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getPublicGroupNames. Gets a list of public group names.
   *
   * @return    A Variant
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public Object getPublicGroupNames  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getOPCServers. Returns an array of Server names on the specified node
   *
   * @param     node A Variant (in, optional, pass null if not required)
   * @return    A Variant
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public Object getOPCServers  (
              Object node) throws java.io.IOException, com.linar.jintegra.AutomationException;

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
              Object node) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * disconnect. Terminate the connection with the OPC Server
   *
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void disconnect  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * createBrowser. Create a new OPCBrowser Object
   *
   * @return    An reference to a cern.c2mon.daq.opcua.jintegraInterface.OPCBrowser
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public cern.c2mon.daq.opcua.jintegraInterface.OPCBrowser createBrowser  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getErrorString. Convert an error code to a descriptive string
   *
   * @param     errorCode The errorCode (in)
   * @return    The errorString
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getErrorString  (
              int errorCode) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * queryAvailableLocaleIDs. Returns the LocaleIDs supported by this server
   *
   * @return    A Variant
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public Object queryAvailableLocaleIDs  () throws java.io.IOException, com.linar.jintegra.AutomationException;

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
              short[][] dataTypes) throws java.io.IOException, com.linar.jintegra.AutomationException;

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
              int[][] errors) throws java.io.IOException, com.linar.jintegra.AutomationException;

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
              int[][] errors) throws java.io.IOException, com.linar.jintegra.AutomationException;


  // Constants to help J-Integra for COM dynamically map DCOM invocations to
  // interface members.  Don't worry, you will never need to explicitly use these constants.
  int IID28e68f92_8d75_11d1_8dc3_3c302a000000 = 1;
  /** Dummy reference to interface proxy to make sure it gets compiled */
  int xxDummy = IOPCAutoServerProxy.xxDummy;
  /** Used internally by J-Integra for COM, please ignore */
  String IID = "28e68f92-8d75-11d1-8dc3-3c302a000000";
  String DISPID_1610743808_GET_NAME = "getStartTime";
  String DISPID_1610743809_GET_NAME = "getCurrentTime";
  String DISPID_1610743810_GET_NAME = "getLastUpdateTime";
  String DISPID_1610743811_GET_NAME = "getMajorVersion";
  String DISPID_1610743812_GET_NAME = "getMinorVersion";
  String DISPID_1610743813_GET_NAME = "getBuildNumber";
  String DISPID_1610743814_GET_NAME = "getVendorInfo";
  String DISPID_1610743815_GET_NAME = "getServerState";
  String DISPID_1610743816_GET_NAME = "getServerName";
  String DISPID_1610743817_GET_NAME = "getServerNode";
  String DISPID_1610743818_GET_NAME = "getClientName";
  String DISPID_1610743818_PUT_NAME = "setClientName";
  String DISPID_1610743820_GET_NAME = "getLocaleID";
  String DISPID_1610743820_PUT_NAME = "setLocaleID";
  String DISPID_1610743822_GET_NAME = "getBandwidth";
  String DISPID_0_GET_NAME = "getOPCGroups";
  String DISPID_1610743824_GET_NAME = "getPublicGroupNames";
  String DISPID_1610743825_NAME = "getOPCServers";
  String DISPID_1610743826_NAME = "connect";
  String DISPID_1610743827_NAME = "disconnect";
  String DISPID_1610743828_NAME = "createBrowser";
  String DISPID_1610743829_NAME = "getErrorString";
  String DISPID_1610743830_NAME = "queryAvailableLocaleIDs";
  String DISPID_1610743831_NAME = "queryAvailableProperties";
  String DISPID_1610743832_NAME = "getItemProperties";
  String DISPID_1610743833_NAME = "lookupItemIDs";
}
