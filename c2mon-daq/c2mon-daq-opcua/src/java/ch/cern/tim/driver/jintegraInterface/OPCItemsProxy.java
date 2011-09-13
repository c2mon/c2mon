package ch.cern.tim.driver.jintegraInterface;

import com.linar.jintegra.*;
/**
 * Proxy for COM Interface 'OPCItems'. Generated 10/5/2006 11:24:10 AM
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
public class OPCItemsProxy extends com.linar.jintegra.Dispatch implements ch.cern.tim.driver.jintegraInterface.OPCItems, java.io.Serializable {

  protected String getJintegraVersion() { return "2.6"; }

  static { ch.cern.tim.driver.jintegraInterface.JIntegraInit.init(); }

  public static final Class targetClass = OPCItems.class;

  public OPCItemsProxy(String CLSID, String host, AuthInfo authInfo) throws java.net.UnknownHostException, java.io.IOException{
    super(CLSID, OPCItems.IID, host, authInfo);
  }

  /** For internal use only */
  public OPCItemsProxy() {}

  public OPCItemsProxy(Object obj) throws java.io.IOException {
    super(obj, OPCItems.IID);
  }

  protected OPCItemsProxy(Object obj, String iid) throws java.io.IOException {
    super(obj, iid);
  }

  protected OPCItemsProxy(String CLSID, String iid, String host, AuthInfo authInfo) throws java.io.IOException {
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
   * getParent. Returns the parent OPCGroup
   *
   * @return    An reference to a ch.cern.tim.driver.jintegraInterface.OPCGroup
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public ch.cern.tim.driver.jintegraInterface.OPCGroup getParent  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    ch.cern.tim.driver.jintegraInterface.OPCGroup zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getParent", 7, zz_parameters);
    return (ch.cern.tim.driver.jintegraInterface.OPCGroup)zz_retVal[0];
  }

  /**
   * getDefaultRequestedDataType. Gets the default requessted data type for the collection.
   *
   * @return    The defaultRequestedDataType
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public short getDefaultRequestedDataType  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    short zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getDefaultRequestedDataType", 8, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * setDefaultRequestedDataType. Gets the default requessted data type for the collection.
   *
   * @param     defaultRequestedDataType The defaultRequestedDataType (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setDefaultRequestedDataType  (
              short defaultRequestedDataType) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Short(defaultRequestedDataType), zz_retVal };
    vtblInvoke("setDefaultRequestedDataType", 9, zz_parameters);
    return;
  }

  /**
   * getDefaultAccessPath. Gets the default access path for the collection.
   *
   * @return    The defaultAccessPath
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public String getDefaultAccessPath  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    String zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getDefaultAccessPath", 10, zz_parameters);
    return (String)zz_retVal[0];
  }

  /**
   * setDefaultAccessPath. Gets the default access path for the collection.
   *
   * @param     defaultAccessPath The defaultAccessPath (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setDefaultAccessPath  (
              String defaultAccessPath) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { defaultAccessPath, zz_retVal };
    vtblInvoke("setDefaultAccessPath", 11, zz_parameters);
    return;
  }

  /**
   * isDefaultIsActive. Gets the default active state for the collection.
   *
   * @return    The defaultIsActive
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public boolean isDefaultIsActive  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    boolean zz_retVal[] = { false };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("isDefaultIsActive", 12, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * setDefaultIsActive. Gets the default active state for the collection.
   *
   * @param     defaultIsActive The defaultIsActive (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setDefaultIsActive  (
              boolean defaultIsActive) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Boolean(defaultIsActive), zz_retVal };
    vtblInvoke("setDefaultIsActive", 13, zz_parameters);
    return;
  }

  /**
   * getCount. Gets the number of items in the Collection
   *
   * @return    The count
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getCount  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    int zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getCount", 14, zz_parameters);
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
    vtblInvoke("get_NewEnum", 15, zz_parameters);
    return (java.util.Enumeration)zz_retVal[0];
  }

  /**
   * item. Returns an OPCItem by index (starts at 1)
   *
   * @param     itemSpecifier A Variant (in)
   * @return    An reference to a ch.cern.tim.driver.jintegraInterface.OPCItem
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public ch.cern.tim.driver.jintegraInterface.OPCItem item  (
              Object itemSpecifier) throws java.io.IOException, com.linar.jintegra.AutomationException{
    ch.cern.tim.driver.jintegraInterface.OPCItem zz_retVal[] = { null };
    Object zz_parameters[] = { itemSpecifier == null ? new Variant("itemSpecifier") : itemSpecifier, zz_retVal };
    vtblInvoke("item", 16, zz_parameters);
    return (ch.cern.tim.driver.jintegraInterface.OPCItem)zz_retVal[0];
  }

  /**
   * getOPCItem. Returns an OPCItem specified by server handle
   *
   * @param     serverHandle The serverHandle (in)
   * @return    An reference to a ch.cern.tim.driver.jintegraInterface.OPCItem
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public ch.cern.tim.driver.jintegraInterface.OPCItem getOPCItem  (
              int serverHandle) throws java.io.IOException, com.linar.jintegra.AutomationException{
    ch.cern.tim.driver.jintegraInterface.OPCItem zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(serverHandle), zz_retVal };
    vtblInvoke("getOPCItem", 17, zz_parameters);
    return (ch.cern.tim.driver.jintegraInterface.OPCItem)zz_retVal[0];
  }

  /**
   * addItem. Adds an OPCItem object to the collection
   *
   * @param     itemID The itemID (in)
   * @param     clientHandle The clientHandle (in)
   * @return    An reference to a ch.cern.tim.driver.jintegraInterface.OPCItem
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public ch.cern.tim.driver.jintegraInterface.OPCItem addItem  (
              String itemID,
              int clientHandle) throws java.io.IOException, com.linar.jintegra.AutomationException{
    ch.cern.tim.driver.jintegraInterface.OPCItem zz_retVal[] = { null };
    Object zz_parameters[] = { itemID, new Integer(clientHandle), zz_retVal };
    vtblInvoke("addItem", 18, zz_parameters);
    return (ch.cern.tim.driver.jintegraInterface.OPCItem)zz_retVal[0];
  }

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
              Object accessPaths) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(numItems), itemIDs, clientHandles, serverHandles, errors, requestedDataTypes == null ? new Variant("requestedDataTypes", Variant.VT_ERROR, Variant.DISP_E_PARAMNOTFOUND) : requestedDataTypes, accessPaths == null ? new Variant("accessPaths", Variant.VT_ERROR, Variant.DISP_E_PARAMNOTFOUND) : accessPaths, zz_retVal };
    vtblInvoke("addItems", 19, zz_parameters);
    return;
  }

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
              int[][] errors) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(numItems), serverHandles, errors, zz_retVal };
    vtblInvoke("remove", 20, zz_parameters);
    return;
  }

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
              Object accessPaths) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(numItems), itemIDs, errors, requestedDataTypes == null ? new Variant("requestedDataTypes", Variant.VT_ERROR, Variant.DISP_E_PARAMNOTFOUND) : requestedDataTypes, accessPaths == null ? new Variant("accessPaths", Variant.VT_ERROR, Variant.DISP_E_PARAMNOTFOUND) : accessPaths, zz_retVal };
    vtblInvoke("validate", 21, zz_parameters);
    return;
  }

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
              int[][] errors) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(numItems), serverHandles, new Boolean(activeState), errors, zz_retVal };
    vtblInvoke("setActive", 22, zz_parameters);
    return;
  }

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
              int[][] errors) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(numItems), serverHandles, clientHandles, errors, zz_retVal };
    vtblInvoke("setClientHandles", 23, zz_parameters);
    return;
  }

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
              int[][] errors) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(numItems), serverHandles, requestedDataTypes, errors, zz_retVal };
    vtblInvoke("setDataTypes", 24, zz_parameters);
    return;
  }

  /** Dummy reference from interface to proxy to make sure proxy gets compiled */
  static final int xxDummy = 0;

  static {
    com.linar.jintegra.InterfaceDesc.add("28e68f98-8d75-11d1-8dc3-3c302a000000", ch.cern.tim.driver.jintegraInterface.OPCItemsProxy.class, null, 7, new com.linar.jintegra.MemberDesc[] {
        new com.linar.jintegra.MemberDesc("getParent",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("ppParent", 29, 20, 5, ch.cern.tim.driver.jintegraInterface.OPCGroup.IID, ch.cern.tim.driver.jintegraInterface.OPCGroup.class) }),
        new com.linar.jintegra.MemberDesc("getDefaultRequestedDataType",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("defaultRequestedDataType", 2, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setDefaultRequestedDataType",
            new Class[] { Short.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("defaultRequestedDataType", 2, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getDefaultAccessPath",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("defaultAccessPath", 8, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setDefaultAccessPath",
            new Class[] { String.class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("defaultAccessPath", 8, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("isDefaultIsActive",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("defaultIsActive", 11, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setDefaultIsActive",
            new Class[] { Boolean.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("defaultIsActive", 11, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
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
              new com.linar.jintegra.Param("ppItem", 29, 20, 4, ch.cern.tim.driver.jintegraInterface.OPCItem.IID, ch.cern.tim.driver.jintegraInterface.OPCItemProxy.class) }),
        new com.linar.jintegra.MemberDesc("getOPCItem",
            new Class[] { Integer.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("serverHandle", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("ppItem", 29, 20, 4, ch.cern.tim.driver.jintegraInterface.OPCItem.IID, ch.cern.tim.driver.jintegraInterface.OPCItemProxy.class) }),
        new com.linar.jintegra.MemberDesc("addItem",
            new Class[] { String.class, Integer.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("itemID", 8, 2, 8, null, null), 
              new com.linar.jintegra.Param("clientHandle", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("ppItem", 29, 20, 4, ch.cern.tim.driver.jintegraInterface.OPCItem.IID, ch.cern.tim.driver.jintegraInterface.OPCItemProxy.class) }),
        new com.linar.jintegra.MemberDesc("addItems",
            new Class[] { Integer.TYPE, String[].class, int[].class, int[][].class, int[][].class, Object.class, Object.class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("numItems", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("itemIDs", 8, 67, 8, null, null), 
              new com.linar.jintegra.Param("clientHandles", 3, 67, 8, null, null), 
              new com.linar.jintegra.Param("serverHandles", 16387, 5, 8, null, null), 
              new com.linar.jintegra.Param("errors", 16387, 5, 8, null, null), 
              new com.linar.jintegra.Param("requestedDataTypes", 12, 10, 8, null, null), 
              new com.linar.jintegra.Param("accessPaths", 12, 10, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("remove",
            new Class[] { Integer.TYPE, int[].class, int[][].class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("numItems", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("serverHandles", 3, 67, 8, null, null), 
              new com.linar.jintegra.Param("errors", 16387, 5, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("validate",
            new Class[] { Integer.TYPE, String[].class, int[][].class, Object.class, Object.class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("numItems", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("itemIDs", 8, 67, 8, null, null), 
              new com.linar.jintegra.Param("errors", 16387, 5, 8, null, null), 
              new com.linar.jintegra.Param("requestedDataTypes", 12, 10, 8, null, null), 
              new com.linar.jintegra.Param("accessPaths", 12, 10, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setActive",
            new Class[] { Integer.TYPE, int[].class, Boolean.TYPE, int[][].class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("numItems", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("serverHandles", 3, 67, 8, null, null), 
              new com.linar.jintegra.Param("activeState", 11, 2, 8, null, null), 
              new com.linar.jintegra.Param("errors", 16387, 5, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setClientHandles",
            new Class[] { Integer.TYPE, int[].class, int[].class, int[][].class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("numItems", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("serverHandles", 3, 67, 8, null, null), 
              new com.linar.jintegra.Param("clientHandles", 3, 67, 8, null, null), 
              new com.linar.jintegra.Param("errors", 16387, 5, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setDataTypes",
            new Class[] { Integer.TYPE, int[].class, int[].class, int[][].class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("numItems", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("serverHandles", 3, 67, 8, null, null), 
              new com.linar.jintegra.Param("requestedDataTypes", 3, 67, 8, null, null), 
              new com.linar.jintegra.Param("errors", 16387, 5, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
});  }
}
