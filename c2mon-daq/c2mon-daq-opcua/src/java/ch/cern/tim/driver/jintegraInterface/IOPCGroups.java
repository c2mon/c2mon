package ch.cern.tim.driver.jintegraInterface;

/**
 * COM Interface 'IOPCGroups'. Generated 10/5/2006 11:24:10 AM
 * from 'G:\Users\t\timoper\Public\opcdaauto.dll'<P>
 * Generated using com2java Version 2.6 Copyright (c) Intrinsyc Software International, Inc.
 * See  <A HREF="http://j-integra.intrinsyc.com/">http://j-integra.intrinsyc.com/</A><P>
 * Description: '<B>Collection of OPC Group objects</B>'
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
public interface IOPCGroups extends java.io.Serializable {
  /**
   * getParent. Returns the parent OPCServer
   *
   * @return    An reference to a ch.cern.tim.driver.jintegraInterface.IOPCAutoServer
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public ch.cern.tim.driver.jintegraInterface.IOPCAutoServer getParent  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * isDefaultGroupIsActive. Gets the default active state for the collection.
   *
   * @return    The defaultGroupIsActive
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public boolean isDefaultGroupIsActive  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * setDefaultGroupIsActive. Gets the default active state for the collection.
   *
   * @param     defaultGroupIsActive The defaultGroupIsActive (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setDefaultGroupIsActive  (
              boolean defaultGroupIsActive) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getDefaultGroupUpdateRate. Gets the default update rate for the collection.
   *
   * @return    The defaultGroupUpdateRate
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getDefaultGroupUpdateRate  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * setDefaultGroupUpdateRate. Gets the default update rate for the collection.
   *
   * @param     defaultGroupUpdateRate The defaultGroupUpdateRate (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setDefaultGroupUpdateRate  (
              int defaultGroupUpdateRate) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getDefaultGroupDeadband. Gets the default deadband for the collection.
   *
   * @return    The defaultGroupDeadband
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public float getDefaultGroupDeadband  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * setDefaultGroupDeadband. Gets the default deadband for the collection.
   *
   * @param     defaultGroupDeadband The defaultGroupDeadband (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setDefaultGroupDeadband  (
              float defaultGroupDeadband) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getDefaultGroupLocaleID. Gets the default locale setting for the collection.
   *
   * @return    The defaultGroupLocaleID
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getDefaultGroupLocaleID  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * setDefaultGroupLocaleID. Gets the default locale setting for the collection.
   *
   * @param     defaultGroupLocaleID The defaultGroupLocaleID (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setDefaultGroupLocaleID  (
              int defaultGroupLocaleID) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getDefaultGroupTimeBias. Gets the default time bias for the collection.
   *
   * @return    The defaultGroupTimeBias
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getDefaultGroupTimeBias  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * setDefaultGroupTimeBias. Gets the default time bias for the collection.
   *
   * @param     defaultGroupTimeBias The defaultGroupTimeBias (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setDefaultGroupTimeBias  (
              int defaultGroupTimeBias) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getCount. Gets number of groups in the collection.
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
   * item. Returns an OPCGroup by index (starts at 1) or name
   *
   * @param     itemSpecifier A Variant (in)
   * @return    An reference to a ch.cern.tim.driver.jintegraInterface.OPCGroup
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public ch.cern.tim.driver.jintegraInterface.OPCGroup item  (
              Object itemSpecifier) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * add. Adds an OPCGroup to the collection
   *
   * @param     name A Variant (in, optional, pass null if not required)
   * @return    An reference to a ch.cern.tim.driver.jintegraInterface.OPCGroup
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public ch.cern.tim.driver.jintegraInterface.IOPCGroup add  (
              Object name) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * getOPCGroup. Returns an OPCGroup specified by server handle or name
   *
   * @param     itemSpecifier A Variant (in)
   * @return    An reference to a ch.cern.tim.driver.jintegraInterface.OPCGroup
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public ch.cern.tim.driver.jintegraInterface.OPCGroup getOPCGroup  (
              Object itemSpecifier) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * removeAll. Remove all groups and their items
   *
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void removeAll  () throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * remove. Removes an OPCGroup specified by server handle or name
   *
   * @param     itemSpecifier A Variant (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void remove  (
              Object itemSpecifier) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * connectPublicGroup. Adds an existing public OPCGroup to the collection
   *
   * @param     name The name (in)
   * @return    An reference to a ch.cern.tim.driver.jintegraInterface.OPCGroup
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public ch.cern.tim.driver.jintegraInterface.OPCGroup connectPublicGroup  (
              String name) throws java.io.IOException, com.linar.jintegra.AutomationException;

  /**
   * removePublicGroup. Removes a public OPCGroup specified by server handle or name
   *
   * @param     itemSpecifier A Variant (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void removePublicGroup  (
              Object itemSpecifier) throws java.io.IOException, com.linar.jintegra.AutomationException;


  // Constants to help J-Integra for COM dynamically map DCOM invocations to
  // interface members.  Don't worry, you will never need to explicitly use these constants.
  int IID28e68f95_8d75_11d1_8dc3_3c302a000000 = 1;
  /** Dummy reference to interface proxy to make sure it gets compiled */
  int xxDummy = IOPCGroupsProxy.xxDummy;
  /** Used internally by J-Integra for COM, please ignore */
  String IID = "28e68f95-8d75-11d1-8dc3-3c302a000000";
  String DISPID_1610743808_GET_NAME = "getParent";
  String DISPID_1610743809_GET_NAME = "isDefaultGroupIsActive";
  String DISPID_1610743809_PUT_NAME = "setDefaultGroupIsActive";
  String DISPID_1610743811_GET_NAME = "getDefaultGroupUpdateRate";
  String DISPID_1610743811_PUT_NAME = "setDefaultGroupUpdateRate";
  String DISPID_1610743813_GET_NAME = "getDefaultGroupDeadband";
  String DISPID_1610743813_PUT_NAME = "setDefaultGroupDeadband";
  String DISPID_1610743815_GET_NAME = "getDefaultGroupLocaleID";
  String DISPID_1610743815_PUT_NAME = "setDefaultGroupLocaleID";
  String DISPID_1610743817_GET_NAME = "getDefaultGroupTimeBias";
  String DISPID_1610743817_PUT_NAME = "setDefaultGroupTimeBias";
  String DISPID_1610743819_GET_NAME = "getCount";
  String DISPID__4_GET_NAME = "get_NewEnum";
  String DISPID_0_NAME = "item";
  String DISPID_1610743822_NAME = "add";
  String DISPID_1610743823_NAME = "getOPCGroup";
  String DISPID_1610743824_NAME = "removeAll";
  String DISPID_1610743825_NAME = "remove";
  String DISPID_1610743826_NAME = "connectPublicGroup";
  String DISPID_1610743827_NAME = "removePublicGroup";
}
