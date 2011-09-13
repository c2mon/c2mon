package ch.cern.tim.driver.jintegraInterface;

/**
 * COM Interface 'OPCBrowser'. Generated 10/5/2006 11:24:10 AM
 * from 'G:\Users\t\timoper\Public\opcdaauto.dll'<P>
 * Generated using com2java Version 2.6 Copyright (c) Intrinsyc Software International, Inc.
 * See  <A HREF="http://j-integra.intrinsyc.com/">http://j-integra.intrinsyc.com/</A><P>
 * Description: '<B>OPC Browser</B>'
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
public interface OPCBrowser extends java.io.Serializable {
  /**
   * getOrganization. Gets the OPC server namespace space type.
   *
   * @return    The organization
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getOrganization  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getFilter. Gets the text filter used when browsing.
   *
   * @return    The filter
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getFilter  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * setFilter. Gets the text filter used when browsing.
   *
   * @param     filter The filter (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setFilter  (
              String filter) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getDataType. Gets the data type filter used when browsing.
   *
   * @return    The dataType
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public short getDataType  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * setDataType. Gets the data type filter used when browsing.
   *
   * @param     dataType The dataType (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setDataType  (
              short dataType) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getAccessRights. Gets the access rights filter used when browsing.
   *
   * @return    The accessRights
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getAccessRights  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * setAccessRights. Gets the access rights filter used when browsing.
   *
   * @param     accessRights The accessRights (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setAccessRights  (
              int accessRights) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getCurrentPosition. Gets the current position in the tree.
   *
   * @return    The currentPosition
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getCurrentPosition  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getCount. Gets the number of items in the collection
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
   * item. An indexer (starts at 1) for the current set of branch or leaf names.
   *
   * @param     itemSpecifier A Variant (in)
   * @return    The item
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String item  (
              Object itemSpecifier) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * showBranches. Returns all branch names that match the current filters.
   *
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void showBranches  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * showLeafs. Returns all leaf names that match the current filters.
   *
   * @param     flat A Variant (in, optional, pass null if not required)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void showLeafs  (
              Object flat) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * moveUp. Move up a level in the tree.
   *
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void moveUp  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * moveToRoot. Move up to the top (root) of the tree.
   *
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void moveToRoot  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * moveDown. Move down into this branch.
   *
   * @param     branch The branch (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void moveDown  (
              String branch) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * moveTo. Move to this absolute position.
   *
   * @param     branches The branches (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void moveTo  (
              String[] branches) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getItemID. Converts a leaf name to an ItemID
   *
   * @param     leaf The leaf (in)
   * @return    The itemID
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getItemID  (
              String leaf) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getAccessPaths. Returns an array of Access Paths for an ItemID
   *
   * @param     itemID The itemID (in)
   * @return    A Variant
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public Object getAccessPaths  (
              String itemID) throws java.io.IOException, com.linar.jintegra.AutomationException;


  // Constants to help J-Integra for COM dynamically map DCOM invocations to
  // interface members.  Don't worry, you will never need to explicitly use these constants.
  int IID28e68f94_8d75_11d1_8dc3_3c302a000000 = 1;
  /** Dummy reference to interface proxy to make sure it gets compiled */
  int xxDummy = OPCBrowserProxy.xxDummy;
  /** Used internally by J-Integra for COM, please ignore */
  String IID = "28e68f94-8d75-11d1-8dc3-3c302a000000";
  String DISPID_1610743808_GET_NAME = "getOrganization";
  String DISPID_1610743809_GET_NAME = "getFilter";
  String DISPID_1610743809_PUT_NAME = "setFilter";
  String DISPID_1610743811_GET_NAME = "getDataType";
  String DISPID_1610743811_PUT_NAME = "setDataType";
  String DISPID_1610743813_GET_NAME = "getAccessRights";
  String DISPID_1610743813_PUT_NAME = "setAccessRights";
  String DISPID_1610743815_GET_NAME = "getCurrentPosition";
  String DISPID_1610743816_GET_NAME = "getCount";
  String DISPID__4_GET_NAME = "get_NewEnum";
  String DISPID_1610743818_NAME = "item";
  String DISPID_1610743819_NAME = "showBranches";
  String DISPID_1610743820_NAME = "showLeafs";
  String DISPID_1610743821_NAME = "moveUp";
  String DISPID_1610743822_NAME = "moveToRoot";
  String DISPID_1610743823_NAME = "moveDown";
  String DISPID_1610743824_NAME = "moveTo";
  String DISPID_1610743825_NAME = "getItemID";
  String DISPID_1610743826_NAME = "getAccessPaths";
}
