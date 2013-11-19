package cern.c2mon.shared.common.datatag.address;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cern.c2mon.shared.common.datatag.address.impl.HardwareAddressImpl;

/**
 * Factory class for creating HardwareAddress objects from XML input.
 *
 * The factory is an implementation of the Singleton pattern. Instances can 
 * only be obtained by calling the static method getInstance().
 *
 * <PRE>
 * HardwareAddressFactory factory = HardwareAddressFactory.getInstance();
 * HardwareAddress addr = factor.fromConfigXML(element);
 * 
 * @author J. Stowisek
 * @version $Revision: 1.2 $ ($Date: 2007/07/04 12:39:13 $ - $State: Exp $)
 */



public abstract class HardwareAddressFactory  {
  /**
   * Get an instance of the HardwareAddressFactory.
   */
  public static HardwareAddressFactory getInstance() {
    return instance;
  }

  /**
   * Create a HardwareAddress object from a DOM Element.
   */
  public abstract HardwareAddress fromConfigXML(Element e);

  /**
   * Create a HardwareAddress object from a DOM Document.
   */
  public abstract HardwareAddress fromConfigXML(Document d);

  /**
   * Create a HardwareAddress object from an XML string.
   */
  public abstract HardwareAddress fromConfigXML(String s);


  /**
   * Create an XML representation of the HardareAddress object passed as a
   * parameter.
   */
  public abstract String toConfigXML(HardwareAddress pAddress);


  /**
   * The singleton instance of the HardwareAddressFactory class created when
   * the class is loaded.
   */
  protected static HardwareAddressFactory instance;

  /**
   * Static initialised creating a singleton instance of the 
   * HardwareAddressFactory class.
   */
  static {
    instance = new HardwareAddressImpl();
  }
}