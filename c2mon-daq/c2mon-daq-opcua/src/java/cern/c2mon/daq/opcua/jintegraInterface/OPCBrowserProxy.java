package cern.c2mon.daq.opcua.jintegraInterface;

import com.linar.jintegra.*;
/**
 * Proxy for COM Interface 'OPCBrowser'. Generated 10/5/2006 11:24:10 AM
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
public class OPCBrowserProxy extends com.linar.jintegra.Dispatch implements cern.c2mon.daq.opcua.jintegraInterface.OPCBrowser, java.io.Serializable {

  protected String getJintegraVersion() { return "2.6"; }

  static { cern.c2mon.daq.opcua.jintegraInterface.JIntegraInit.init(); }

  public static final Class targetClass = OPCBrowser.class;

  public OPCBrowserProxy(String CLSID, String host, AuthInfo authInfo) throws java.net.UnknownHostException, java.io.IOException{
    super(CLSID, OPCBrowser.IID, host, authInfo);
  }

  /** For internal use only */
  public OPCBrowserProxy() {}

  public OPCBrowserProxy(Object obj) throws java.io.IOException {
    super(obj, OPCBrowser.IID);
  }

  protected OPCBrowserProxy(Object obj, String iid) throws java.io.IOException {
    super(obj, iid);
  }

  protected OPCBrowserProxy(String CLSID, String iid, String host, AuthInfo authInfo) throws java.io.IOException {
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
   * getOrganization. Gets the OPC server namespace space type.
   *
   * @return    The organization
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getOrganization  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    int zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getOrganization", 7, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * getFilter. Gets the text filter used when browsing.
   *
   * @return    The filter
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getFilter  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    String zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getFilter", 8, zz_parameters);
    return (String)zz_retVal[0];
  }

  /**
   * setFilter. Gets the text filter used when browsing.
   *
   * @param     filter The filter (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setFilter  (
              String filter) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { filter, zz_retVal };
    vtblInvoke("setFilter", 9, zz_parameters);
    return;
  }

  /**
   * getDataType. Gets the data type filter used when browsing.
   *
   * @return    The dataType
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public short getDataType  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    short zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getDataType", 10, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * setDataType. Gets the data type filter used when browsing.
   *
   * @param     dataType The dataType (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setDataType  (
              short dataType) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Short(dataType), zz_retVal };
    vtblInvoke("setDataType", 11, zz_parameters);
    return;
  }

  /**
   * getAccessRights. Gets the access rights filter used when browsing.
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
   * setAccessRights. Gets the access rights filter used when browsing.
   *
   * @param     accessRights The accessRights (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setAccessRights  (
              int accessRights) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(accessRights), zz_retVal };
    vtblInvoke("setAccessRights", 13, zz_parameters);
    return;
  }

  /**
   * getCurrentPosition. Gets the current position in the tree.
   *
   * @return    The currentPosition
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getCurrentPosition  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    String zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getCurrentPosition", 14, zz_parameters);
    return (String)zz_retVal[0];
  }

  /**
   * getCount. Gets the number of items in the collection
   *
   * @return    The count
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getCount  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    int zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getCount", 15, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * get_NewEnum. 
   *
   * @return    An enumeration.
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public java.util.Enumeration get_NewEnum  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    java.util.Enumeration zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("get_NewEnum", 16, zz_parameters);
    return (java.util.Enumeration)zz_retVal[0];
  }

  /**
   * item. An indexer (starts at 1) for the current set of branch or leaf names.
   *
   * @param     itemSpecifier A Variant (in)
   * @return    The item
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String item  (
              Object itemSpecifier) throws java.io.IOException, com.linar.jintegra.AutomationException{
    String zz_retVal[] = { null };
    Object zz_parameters[] = { itemSpecifier == null ? new Variant("itemSpecifier") : itemSpecifier, zz_retVal };
    vtblInvoke("item", 17, zz_parameters);
    return (String)zz_retVal[0];
  }

  /**
   * showBranches. Returns all branch names that match the current filters.
   *
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void showBranches  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("showBranches", 18, zz_parameters);
    return;
  }

  /**
   * showLeafs. Returns all leaf names that match the current filters.
   *
   * @param     flat A Variant (in, optional, pass null if not required)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void showLeafs  (
              Object flat) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { flat == null ? new Variant("flat", Variant.VT_ERROR, Variant.DISP_E_PARAMNOTFOUND) : flat, zz_retVal };
    vtblInvoke("showLeafs", 19, zz_parameters);
    return;
  }

  /**
   * moveUp. Move up a level in the tree.
   *
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void moveUp  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("moveUp", 20, zz_parameters);
    return;
  }

  /**
   * moveToRoot. Move up to the top (root) of the tree.
   *
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void moveToRoot  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("moveToRoot", 21, zz_parameters);
    return;
  }

  /**
   * moveDown. Move down into this branch.
   *
   * @param     branch The branch (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void moveDown  (
              String branch) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { branch, zz_retVal };
    vtblInvoke("moveDown", 22, zz_parameters);
    return;
  }

  /**
   * moveTo. Move to this absolute position.
   *
   * @param     branches The branches (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void moveTo  (
              String[] branches) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { branches, zz_retVal };
    vtblInvoke("moveTo", 23, zz_parameters);
    return;
  }

  /**
   * getItemID. Converts a leaf name to an ItemID
   *
   * @param     leaf The leaf (in)
   * @return    The itemID
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getItemID  (
              String leaf) throws java.io.IOException, com.linar.jintegra.AutomationException{
    String zz_retVal[] = { null };
    Object zz_parameters[] = { leaf, zz_retVal };
    vtblInvoke("getItemID", 24, zz_parameters);
    return (String)zz_retVal[0];
  }

  /**
   * getAccessPaths. Returns an array of Access Paths for an ItemID
   *
   * @param     itemID The itemID (in)
   * @return    A Variant
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public Object getAccessPaths  (
              String itemID) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { itemID, zz_retVal };
    vtblInvoke("getAccessPaths", 25, zz_parameters);
    return (Object)zz_retVal[0];
  }

  /** Dummy reference from interface to proxy to make sure proxy gets compiled */
  static final int xxDummy = 0;

  static {
    com.linar.jintegra.InterfaceDesc.add("28e68f94-8d75-11d1-8dc3-3c302a000000", cern.c2mon.daq.opcua.jintegraInterface.OPCBrowserProxy.class, null, 7, new com.linar.jintegra.MemberDesc[] {
        new com.linar.jintegra.MemberDesc("getOrganization",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("organization", 3, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getFilter",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("filter", 8, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setFilter",
            new Class[] { String.class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("filter", 8, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getDataType",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("dataType", 2, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setDataType",
            new Class[] { Short.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("dataType", 2, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getAccessRights",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("accessRights", 3, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setAccessRights",
            new Class[] { Integer.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("accessRights", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getCurrentPosition",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("currentPosition", 8, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getCount",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("count", 3, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("get_NewEnum",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("ppUnk", 13, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("item",
            new Class[] { Object.class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("itemSpecifier", 12, 2, 8, null, null), 
              new com.linar.jintegra.Param("item", 8, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("showBranches",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("showLeafs",
            new Class[] { Object.class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("flat", 12, 10, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("moveUp",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("moveToRoot",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("moveDown",
            new Class[] { String.class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("branch", 8, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("moveTo",
            new Class[] { String[].class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("branches", 8, 67, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getItemID",
            new Class[] { String.class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("leaf", 8, 2, 8, null, null), 
              new com.linar.jintegra.Param("itemID", 8, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getAccessPaths",
            new Class[] { String.class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("itemID", 8, 2, 8, null, null), 
              new com.linar.jintegra.Param("accessPaths", 12, 20, 8, null, null) }),
});  }
}
