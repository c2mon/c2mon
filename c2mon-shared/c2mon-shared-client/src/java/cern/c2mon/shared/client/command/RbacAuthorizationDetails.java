package cern.c2mon.shared.client.command;

import cern.c2mon.shared.common.command.AuthorizationDetails;

/**
 * Implementation of AuthorizationDetails for RBAC, where a class,
 * device and property are associated to every command.
 * 
 * @author Mark Brightwell
 *
 */
public class RbacAuthorizationDetails implements AuthorizationDetails, Cloneable {

  /**
   * This class is sent to the client as a serialized object.
   */
  private static final long serialVersionUID = -9138273440941101293L;

  /**
   * RBAC class.
   */
  private String rbacClass;
  
  /**
   * RBAC device.
   */
  private String rbacDevice;
  
  /**
   * RBAC property.
   */
  private String rbacProperty;
  
  @Override
  public AuthorizationDetails fromJson(String json) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @return the rbacClass
   */
  public String getRbacClass() {
    return rbacClass;
  }

  /**
   * @param rbacClass the rbacClass to set
   */
  public void setRbacClass(final String rbacClass) {
    this.rbacClass = rbacClass;
  }

  /**
   * @return the rbacDevice
   */
  public String getRbacDevice() {
    return rbacDevice;
  }
  
  /**
   * Checks whether the authorization details is null or not.
   * @return true if class - device - property are all null,
   * false otherwise.
   */
  public boolean isEmpty() {
    
    if (getRbacClass() == null
        && getRbacDevice() == null 
          && getRbacProperty() == null)
      return true;
    
    return false;
  }

  /**
   * @param rbacDevice the rbacDevice to set
   */
  public void setRbacDevice(final String rbacDevice) {
    this.rbacDevice = rbacDevice;
  }

  /**
   * @return the rbacProperty
   */
  public String getRbacProperty() {
    return rbacProperty;
  }

  /**
   * @param rbacProperty the rbacProperty to set
   */
  public void setRbacProperty(final String rbacProperty) {
    this.rbacProperty = rbacProperty;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((rbacClass == null) ? 0 : rbacClass.hashCode());
    result = prime * result + ((rbacDevice == null) ? 0 : rbacDevice.hashCode());
    result = prime * result + ((rbacProperty == null) ? 0 : rbacProperty.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RbacAuthorizationDetails other = (RbacAuthorizationDetails) obj;
    if (rbacClass == null) {
      if (other.rbacClass != null)
        return false;
    } else if (!rbacClass.equals(other.rbacClass))
      return false;
    if (rbacDevice == null) {
      if (other.rbacDevice != null)
        return false;
    } else if (!rbacDevice.equals(other.rbacDevice))
      return false;
    if (rbacProperty == null) {
      if (other.rbacProperty != null)
        return false;
    } else if (!rbacProperty.equals(other.rbacProperty))
      return false;
    return true;
  }

  @Override
  public RbacAuthorizationDetails clone() throws CloneNotSupportedException {
    return (RbacAuthorizationDetails) super.clone();
  }
  
  

}
