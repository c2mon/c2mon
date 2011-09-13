package ch.cern.tim.driver.jintegraInterface;

import com.linar.jintegra.*;
/**
 * Proxy for COM Interface 'IOPCGroups'. Generated 10/5/2006 11:24:10 AM
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
public class IOPCGroupsProxy extends com.linar.jintegra.Dispatch implements ch.cern.tim.driver.jintegraInterface.IOPCGroups, java.io.Serializable {

  protected String getJintegraVersion() { return "2.6"; }

  static { ch.cern.tim.driver.jintegraInterface.JIntegraInit.init(); }

  public static final Class targetClass = IOPCGroups.class;

  public IOPCGroupsProxy(String CLSID, String host, AuthInfo authInfo) throws java.net.UnknownHostException, java.io.IOException{
    super(CLSID, IOPCGroups.IID, host, authInfo);
  }

  /** For internal use only */
  public IOPCGroupsProxy() {}

  public IOPCGroupsProxy(Object obj) throws java.io.IOException {
    super(obj, IOPCGroups.IID);
  }

  protected IOPCGroupsProxy(Object obj, String iid) throws java.io.IOException {
    super(obj, iid);
  }

  protected IOPCGroupsProxy(String CLSID, String iid, String host, AuthInfo authInfo) throws java.io.IOException {
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
   * getParent. Returns the parent OPCServer
   *
   * @return    An reference to a ch.cern.tim.driver.jintegraInterface.IOPCAutoServer
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public ch.cern.tim.driver.jintegraInterface.IOPCAutoServer getParent  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    ch.cern.tim.driver.jintegraInterface.IOPCAutoServer zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getParent", 7, zz_parameters);
    return (ch.cern.tim.driver.jintegraInterface.IOPCAutoServer)zz_retVal[0];
  }

  /**
   * isDefaultGroupIsActive. Gets the default active state for the collection.
   *
   * @return    The defaultGroupIsActive
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public boolean isDefaultGroupIsActive  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    boolean zz_retVal[] = { false };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("isDefaultGroupIsActive", 8, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * setDefaultGroupIsActive. Gets the default active state for the collection.
   *
   * @param     defaultGroupIsActive The defaultGroupIsActive (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setDefaultGroupIsActive  (
              boolean defaultGroupIsActive) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Boolean(defaultGroupIsActive), zz_retVal };
    vtblInvoke("setDefaultGroupIsActive", 9, zz_parameters);
    return;
  }

  /**
   * getDefaultGroupUpdateRate. Gets the default update rate for the collection.
   *
   * @return    The defaultGroupUpdateRate
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getDefaultGroupUpdateRate  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    int zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getDefaultGroupUpdateRate", 10, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * setDefaultGroupUpdateRate. Gets the default update rate for the collection.
   *
   * @param     defaultGroupUpdateRate The defaultGroupUpdateRate (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setDefaultGroupUpdateRate  (
              int defaultGroupUpdateRate) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(defaultGroupUpdateRate), zz_retVal };
    vtblInvoke("setDefaultGroupUpdateRate", 11, zz_parameters);
    return;
  }

  /**
   * getDefaultGroupDeadband. Gets the default deadband for the collection.
   *
   * @return    The defaultGroupDeadband
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public float getDefaultGroupDeadband  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    float zz_retVal[] = { 0.0F };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getDefaultGroupDeadband", 12, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * setDefaultGroupDeadband. Gets the default deadband for the collection.
   *
   * @param     defaultGroupDeadband The defaultGroupDeadband (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setDefaultGroupDeadband  (
              float defaultGroupDeadband) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Float(defaultGroupDeadband), zz_retVal };
    vtblInvoke("setDefaultGroupDeadband", 13, zz_parameters);
    return;
  }

  /**
   * getDefaultGroupLocaleID. Gets the default locale setting for the collection.
   *
   * @return    The defaultGroupLocaleID
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getDefaultGroupLocaleID  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    int zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getDefaultGroupLocaleID", 14, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * setDefaultGroupLocaleID. Gets the default locale setting for the collection.
   *
   * @param     defaultGroupLocaleID The defaultGroupLocaleID (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setDefaultGroupLocaleID  (
              int defaultGroupLocaleID) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(defaultGroupLocaleID), zz_retVal };
    vtblInvoke("setDefaultGroupLocaleID", 15, zz_parameters);
    return;
  }

  /**
   * getDefaultGroupTimeBias. Gets the default time bias for the collection.
   *
   * @return    The defaultGroupTimeBias
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getDefaultGroupTimeBias  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    int zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getDefaultGroupTimeBias", 16, zz_parameters);
    return zz_retVal[0];
  }

  /**
   * setDefaultGroupTimeBias. Gets the default time bias for the collection.
   *
   * @param     defaultGroupTimeBias The defaultGroupTimeBias (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void setDefaultGroupTimeBias  (
              int defaultGroupTimeBias) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { new Integer(defaultGroupTimeBias), zz_retVal };
    vtblInvoke("setDefaultGroupTimeBias", 17, zz_parameters);
    return;
  }

  /**
   * getCount. Gets number of groups in the collection.
   *
   * @return    The count
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public int getCount  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    int zz_retVal[] = { 0 };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("getCount", 18, zz_parameters);
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
    vtblInvoke("get_NewEnum", 19, zz_parameters);
    return (java.util.Enumeration)zz_retVal[0];
  }

  /**
   * item. Returns an OPCGroup by index (starts at 1) or name
   *
   * @param     itemSpecifier A Variant (in)
   * @return    An reference to a ch.cern.tim.driver.jintegraInterface.OPCGroup
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public ch.cern.tim.driver.jintegraInterface.OPCGroup item  (
              Object itemSpecifier) throws java.io.IOException, com.linar.jintegra.AutomationException{
    ch.cern.tim.driver.jintegraInterface.OPCGroup zz_retVal[] = { null };
    Object zz_parameters[] = { itemSpecifier == null ? new Variant("itemSpecifier") : itemSpecifier, zz_retVal };
    vtblInvoke("item", 20, zz_parameters);
    return (ch.cern.tim.driver.jintegraInterface.OPCGroup)zz_retVal[0];
  }

  /**
   * add. Adds an OPCGroup to the collection
   *
   * @param     name A Variant (in, optional, pass null if not required)
   * @return    An reference to a ch.cern.tim.driver.jintegraInterface.OPCGroup
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public ch.cern.tim.driver.jintegraInterface.OPCGroup add  (
              Object name) throws java.io.IOException, com.linar.jintegra.AutomationException{
    ch.cern.tim.driver.jintegraInterface.OPCGroup zz_retVal[] = { null };
    Object zz_parameters[] = { name == null ? new Variant("name", Variant.VT_ERROR, Variant.DISP_E_PARAMNOTFOUND) : name, zz_retVal };
    vtblInvoke("add", 21, zz_parameters);
    return (ch.cern.tim.driver.jintegraInterface.OPCGroup)zz_retVal[0];
  }

  /**
   * getOPCGroup. Returns an OPCGroup specified by server handle or name
   *
   * @param     itemSpecifier A Variant (in)
   * @return    An reference to a ch.cern.tim.driver.jintegraInterface.OPCGroup
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public ch.cern.tim.driver.jintegraInterface.OPCGroup getOPCGroup  (
              Object itemSpecifier) throws java.io.IOException, com.linar.jintegra.AutomationException{
    ch.cern.tim.driver.jintegraInterface.OPCGroup zz_retVal[] = { null };
    Object zz_parameters[] = { itemSpecifier == null ? new Variant("itemSpecifier") : itemSpecifier, zz_retVal };
    vtblInvoke("getOPCGroup", 22, zz_parameters);
    return (ch.cern.tim.driver.jintegraInterface.OPCGroup)zz_retVal[0];
  }

  /**
   * removeAll. Remove all groups and their items
   *
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void removeAll  () throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { zz_retVal };
    vtblInvoke("removeAll", 23, zz_parameters);
    return;
  }

  /**
   * remove. Removes an OPCGroup specified by server handle or name
   *
   * @param     itemSpecifier A Variant (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void remove  (
              Object itemSpecifier) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { itemSpecifier == null ? new Variant("itemSpecifier") : itemSpecifier, zz_retVal };
    vtblInvoke("remove", 24, zz_parameters);
    return;
  }

  /**
   * connectPublicGroup. Adds an existing public OPCGroup to the collection
   *
   * @param     name The name (in)
   * @return    An reference to a ch.cern.tim.driver.jintegraInterface.OPCGroup
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public ch.cern.tim.driver.jintegraInterface.OPCGroup connectPublicGroup  (
              String name) throws java.io.IOException, com.linar.jintegra.AutomationException{
    ch.cern.tim.driver.jintegraInterface.OPCGroup zz_retVal[] = { null };
    Object zz_parameters[] = { name, zz_retVal };
    vtblInvoke("connectPublicGroup", 25, zz_parameters);
    return (ch.cern.tim.driver.jintegraInterface.OPCGroup)zz_retVal[0];
  }

  /**
   * removePublicGroup. Removes a public OPCGroup specified by server handle or name
   *
   * @param     itemSpecifier A Variant (in)
   * @exception java.io.IOException If there are communications problems.
   * @exception com.linar.jintegra.AutomationException If the remote server throws an exception.
   */
  public void removePublicGroup  (
              Object itemSpecifier) throws java.io.IOException, com.linar.jintegra.AutomationException{
    Object zz_retVal[] = { null };
    Object zz_parameters[] = { itemSpecifier == null ? new Variant("itemSpecifier") : itemSpecifier, zz_retVal };
    vtblInvoke("removePublicGroup", 26, zz_parameters);
    return;
  }

  /** Dummy reference from interface to proxy to make sure proxy gets compiled */
  static final int xxDummy = 0;

  static {
    com.linar.jintegra.InterfaceDesc.add("28e68f95-8d75-11d1-8dc3-3c302a000000", ch.cern.tim.driver.jintegraInterface.IOPCGroupsProxy.class, null, 7, new com.linar.jintegra.MemberDesc[] {
        new com.linar.jintegra.MemberDesc("getParent",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("ppParent", 29, 20, 4, ch.cern.tim.driver.jintegraInterface.IOPCAutoServer.IID, ch.cern.tim.driver.jintegraInterface.IOPCAutoServerProxy.class) }),
        new com.linar.jintegra.MemberDesc("isDefaultGroupIsActive",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("defaultGroupIsActive", 11, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setDefaultGroupIsActive",
            new Class[] { Boolean.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("defaultGroupIsActive", 11, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getDefaultGroupUpdateRate",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("defaultGroupUpdateRate", 3, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setDefaultGroupUpdateRate",
            new Class[] { Integer.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("defaultGroupUpdateRate", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getDefaultGroupDeadband",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("defaultGroupDeadband", 4, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setDefaultGroupDeadband",
            new Class[] { Float.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("defaultGroupDeadband", 4, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getDefaultGroupLocaleID",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("defaultGroupLocaleID", 3, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setDefaultGroupLocaleID",
            new Class[] { Integer.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("defaultGroupLocaleID", 3, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("getDefaultGroupTimeBias",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("defaultGroupTimeBias", 3, 20, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("setDefaultGroupTimeBias",
            new Class[] { Integer.TYPE, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("defaultGroupTimeBias", 3, 2, 8, null, null), 
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
              new com.linar.jintegra.Param("ppGroup", 29, 20, 5, ch.cern.tim.driver.jintegraInterface.OPCGroup.IID, ch.cern.tim.driver.jintegraInterface.OPCGroup.class) }),
        new com.linar.jintegra.MemberDesc("add",
            new Class[] { Object.class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("name", 12, 10, 8, null, null), 
              new com.linar.jintegra.Param("ppGroup", 29, 20, 5, ch.cern.tim.driver.jintegraInterface.OPCGroup.IID, ch.cern.tim.driver.jintegraInterface.OPCGroup.class) }),
        new com.linar.jintegra.MemberDesc("getOPCGroup",
            new Class[] { Object.class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("itemSpecifier", 12, 2, 8, null, null), 
              new com.linar.jintegra.Param("ppGroup", 29, 20, 5, ch.cern.tim.driver.jintegraInterface.OPCGroup.IID, ch.cern.tim.driver.jintegraInterface.OPCGroup.class) }),
        new com.linar.jintegra.MemberDesc("removeAll",
            new Class[] { },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("remove",
            new Class[] { Object.class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("itemSpecifier", 12, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
        new com.linar.jintegra.MemberDesc("connectPublicGroup",
            new Class[] { String.class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("name", 8, 2, 8, null, null), 
              new com.linar.jintegra.Param("ppGroup", 29, 20, 5, ch.cern.tim.driver.jintegraInterface.OPCGroup.IID, ch.cern.tim.driver.jintegraInterface.OPCGroup.class) }),
        new com.linar.jintegra.MemberDesc("removePublicGroup",
            new Class[] { Object.class, },
            new com.linar.jintegra.Param[] { 
              new com.linar.jintegra.Param("itemSpecifier", 12, 2, 8, null, null), 
              new com.linar.jintegra.Param("", 24, 0, 8, null, null) }),
});  }
}
