package cern.c2mon.client.common.config;

public class ConfigurationProperty 
{
  private String propertyName = null;
  private String propertyType = null;
  private Object propertyValue = null;

  public ConfigurationProperty(String pName, Object pValue, String pType)
  {
    propertyName = pName;
    propertyValue = pValue;
    propertyType = pType;
  }
  
  public ConfigurationProperty(String pName, Object pValue)
  {
    this(pName, pValue, null);
  }
  
  public String getName()
  {
    return propertyName;
  }
  
  public String getType()
  {
    return propertyType;
  }
  
  public Object getValue()
  {
    return propertyValue;
  }  
}