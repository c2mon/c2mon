package cern.c2mon.daq.opcua.jintegraInterface;

/**
 * COM Interface 'OPCItems'. Generated 10/5/2006 11:24:10 AM
 * from 'G:\Users\t\timoper\Public\opcdaauto.dll'<P>
 * Generated using com2java Version 2.6 Copyright (c) Intrinsyc Software International, Inc.
 * See  <A HREF="http://j-integra.intrinsyc.com/">http://j-integra.intrinsyc.com/</A><P>
 * Description: '<B>Collection of OPC Item objects</B>'
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
public interface OPCItems extends java.io.Serializable {
  /**
   * getParent. Returns the parent OPCGroup
   *
   * @return    An reference to a cern.c2mon.daq.opcua.jintegraInterface.OPCGroup
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public cern.c2mon.daq.opcua.jintegraInterface.OPCGroup getParent  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getDefaultRequestedDataType. Gets the default requessted data type for the collection.
   *
   * @return    The defaultRequestedDataType
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public short getDefaultRequestedDataType  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * setDefaultRequestedDataType. Gets the default requessted data type for the collection.
   *
   * @param     defaultRequestedDataType The defaultRequestedDataType (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setDefaultRequestedDataType  (
              short defaultRequestedDataType) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getDefaultAccessPath. Gets the default access path for the collection.
   *
   * @return    The defaultAccessPath
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getDefaultAccessPath  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * setDefaultAccessPath. Gets the default access path for the collection.
   *
   * @param     defaultAccessPath The defaultAccessPath (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setDefaultAccessPath  (
              String defaultAccessPath) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * isDefaultIsActive. Gets the default active state for the collection.
   *
   * @return    The defaultIsActive
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public boolean isDefaultIsActive  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * setDefaultIsActive. Gets the default active state for the collection.
   *
   * @param     defaultIsActive The defaultIsActive (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setDefaultIsActive  (
              boolean defaultIsActive) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getCount. Gets the number of items in the Collection
   *
   * @return    The count
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getCount  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * get_NewEnum. 
   *
   * @return    An enumeration.
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public java.util.Enumeration get_NewEnum  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * item. Returns an OPCItem by index (starts at 1)
   *
   * @param     itemSpecifier A Variant (in)
   * @return    An reference to a cern.c2mon.daq.opcua.jintegraInterface.OPCItem
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public cern.c2mon.daq.opcua.jintegraInterface.OPCItem item  (
              Object itemSpecifier) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getOPCItem. Returns an OPCItem specified by server handle
   *
   * @param     serverHandle The serverHandle (in)
   * @return    An reference to a cern.c2mon.daq.opcua.jintegraInterface.OPCItem
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public cern.c2mon.daq.opcua.jintegraInterface.OPCItem getOPCItem  (
              int serverHandle) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * addItem. Adds an OPCItem object to the collection
   *
   * @param     itemID The itemID (in)
   * @param     clientHandle The clientHandle (in)
   * @return    An reference to a cern.c2mon.daq.opcua.jintegraInterface.OPCItem
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public cern.c2mon.daq.opcua.jintegraInterface.OPCItem addItem  (
              String itemID,
              int clientHandle) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * addItems. Adds OPCItem objects to the collection
   *
   * @param     numItems The numItems (in)
   * @param     itemIDs The itemIDs (in)
   * @param     clientHandles The clientHandles (in)
   * @param     serverHandles The serverHandles (out: use single element array)
   * @param     errors The errors (out: use single element array)
   * @param     requestedDataTypes A Variant (in, optional, pass null if not required)
   * @param     accessPaths A Variant (in, optional, pass null if not required)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void addItems  (
              int numItems,
              String[] itemIDs,
              int[] clientHandles,
              int[][] serverHandles,
              int[][] errors,
              Object requestedDataTypes,
              Object accessPaths) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * remove. Removes OPCItem objects from the collection
   *
   * @param     numItems The numItems (in)
   * @param     serverHandles The serverHandles (in)
   * @param     errors The errors (out: use single element array)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void remove  (
              int numItems,
              int[] serverHandles,
              int[][] errors) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * validate. Validates a set of item ids without adding them to the collection.
   *
   * @param     numItems The numItems (in)
   * @param     itemIDs The itemIDs (in)
   * @param     errors The errors (out: use single element array)
   * @param     requestedDataTypes A Variant (in, optional, pass null if not required)
   * @param     accessPaths A Variant (in, optional, pass null if not required)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void validate  (
              int numItems,
              String[] itemIDs,
              int[][] errors,
              Object requestedDataTypes,
              Object accessPaths) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * setActive. Set the active state of OPCItem objects
   *
   * @param     numItems The numItems (in)
   * @param     serverHandles The serverHandles (in)
   * @param     activeState The activeState (in)
   * @param     errors The errors (out: use single element array)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setActive  (
              int numItems,
              int[] serverHandles,
              boolean activeState,
              int[][] errors) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * setClientHandles. Set the Client handles of OPCItem objects
   *
   * @param     numItems The numItems (in)
   * @param     serverHandles The serverHandles (in)
   * @param     clientHandles The clientHandles (in)
   * @param     errors The errors (out: use single element array)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setClientHandles  (
              int numItems,
              int[] serverHandles,
              int[] clientHandles,
              int[][] errors) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * setDataTypes. Set the Data Types of OPCItem objects
   *
   * @param     numItems The numItems (in)
   * @param     serverHandles The serverHandles (in)
   * @param     requestedDataTypes The requestedDataTypes (in)
   * @param     errors The errors (out: use single element array)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setDataTypes  (
              int numItems,
              int[] serverHandles,
              int[] requestedDataTypes,
              int[][] errors) throws java.io.IOException, com.linar.jintegra.AutomationException;


  // Constants to help J-Integra for COM dynamically map DCOM invocations to
  // interface members.  Don't worry, you will never need to explicitly use these constants.
  int IID28e68f98_8d75_11d1_8dc3_3c302a000000 = 1;
  /** Dummy reference to interface proxy to make sure it gets compiled */
  int xxDummy = OPCItemsProxy.xxDummy;
  /** Used internally by J-Integra for COM, please ignore */
  String IID = "28e68f98-8d75-11d1-8dc3-3c302a000000";
  String DISPID_1610743808_GET_NAME = "getParent";
  String DISPID_1610743809_GET_NAME = "getDefaultRequestedDataType";
  String DISPID_1610743809_PUT_NAME = "setDefaultRequestedDataType";
  String DISPID_1610743811_GET_NAME = "getDefaultAccessPath";
  String DISPID_1610743811_PUT_NAME = "setDefaultAccessPath";
  String DISPID_1610743813_GET_NAME = "isDefaultIsActive";
  String DISPID_1610743813_PUT_NAME = "setDefaultIsActive";
  String DISPID_1610743815_GET_NAME = "getCount";
  String DISPID__4_GET_NAME = "get_NewEnum";
  String DISPID_0_NAME = "item";
  String DISPID_1610743818_NAME = "getOPCItem";
  String DISPID_1610743819_NAME = "addItem";
  String DISPID_1610743820_NAME = "addItems";
  String DISPID_1610743821_NAME = "remove";
  String DISPID_1610743822_NAME = "validate";
  String DISPID_1610743823_NAME = "setActive";
  String DISPID_1610743824_NAME = "setClientHandles";
  String DISPID_1610743825_NAME = "setDataTypes";
}
