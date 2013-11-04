package cern.c2mon.daq.opcua.jintegraInterface;

/**
 * COM Interface 'OPCItem'. Generated 10/5/2006 11:24:10 AM
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
public interface OPCItem extends java.io.Serializable {
  /**
   * getParent. Returns the parent OPCGroup.
   *
   * @return    An reference to a cern.c2mon.daq.opcua.jintegraInterface.OPCGroup
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public cern.c2mon.daq.opcua.jintegraInterface.OPCGroup getParent  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getClientHandle. Gets the item client handle.
   *
   * @return    The clientHandle
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getClientHandle  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * setClientHandle. Gets the item client handle.
   *
   * @param     clientHandle The clientHandle (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setClientHandle  (
              int clientHandle) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getServerHandle. Gets the item server handle.
   *
   * @return    The serverHandle
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getServerHandle  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getAccessPath. Gets the item access path.
   *
   * @return    The accessPath
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getAccessPath  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getAccessRights. Gets the item access rights.
   *
   * @return    The accessRights
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getAccessRights  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getItemID. Gets the item id.
   *
   * @return    The itemID
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getItemID  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * isActive. Gets the item active state.
   *
   * @return    The isActive
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public boolean isActive  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * setIsActive. Gets the item active state.
   *
   * @param     isActive The isActive (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setIsActive  (
              boolean isActive) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getRequestedDataType. Gets the item requested data type.
   *
   * @return    The requestedDataType
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public short getRequestedDataType  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * setRequestedDataType. Gets the item requested data type.
   *
   * @param     requestedDataType The requestedDataType (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setRequestedDataType  (
              short requestedDataType) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getValue. Gets the current item value.
   *
   * @return    A Variant
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public Object getValue  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getQuality. Gets the current item value quality.
   *
   * @return    The quality
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getQuality  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getTimeStamp. Gets the current item value timestamp.
   *
   * @return    The timeStamp
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public java.util.Date getTimeStamp  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getCanonicalDataType. Gets the item canonical data type.
   *
   * @return    The canonicalDataType
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public short getCanonicalDataType  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getEUType. Gets the item EU type.
   *
   * @return    The eUType
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public short getEUType  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getEUInfo. Gets the item EU info.
   *
   * @return    A Variant
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public Object getEUInfo  () throws java.io.IOException, com.linar.jintegra.AutomationException;

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
              Object[] timeStamp) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * write. 
   *
   * @param     value A Variant (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void write  (
              Object value) throws java.io.IOException, com.linar.jintegra.AutomationException;


  // Constants to help J-Integra for COM dynamically map DCOM invocations to
  // interface members.  Don't worry, you will never need to explicitly use these constants.
  int IID28e68f99_8d75_11d1_8dc3_3c302a000000 = 1;
  /** Dummy reference to interface proxy to make sure it gets compiled */
  int xxDummy = OPCItemProxy.xxDummy;
  /** Used internally by J-Integra for COM, please ignore */
  String IID = "28e68f99-8d75-11d1-8dc3-3c302a000000";
  String DISPID_1610743808_GET_NAME = "getParent";
  String DISPID_1610743809_GET_NAME = "getClientHandle";
  String DISPID_1610743809_PUT_NAME = "setClientHandle";
  String DISPID_1610743811_GET_NAME = "getServerHandle";
  String DISPID_1610743812_GET_NAME = "getAccessPath";
  String DISPID_1610743813_GET_NAME = "getAccessRights";
  String DISPID_1610743814_GET_NAME = "getItemID";
  String DISPID_1610743815_GET_NAME = "isActive";
  String DISPID_1610743815_PUT_NAME = "setIsActive";
  String DISPID_1610743817_GET_NAME = "getRequestedDataType";
  String DISPID_1610743817_PUT_NAME = "setRequestedDataType";
  String DISPID_0_GET_NAME = "getValue";
  String DISPID_1610743820_GET_NAME = "getQuality";
  String DISPID_1610743821_GET_NAME = "getTimeStamp";
  String DISPID_1610743822_GET_NAME = "getCanonicalDataType";
  String DISPID_1610743823_GET_NAME = "getEUType";
  String DISPID_1610743824_GET_NAME = "getEUInfo";
  String DISPID_1610743825_NAME = "read";
  String DISPID_1610743826_NAME = "write";
}
