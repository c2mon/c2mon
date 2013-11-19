package cern.c2mon.shared.common.command;

import java.io.Serializable;

/**
 * Should be implemented by any class containing authorization details
 * that need sending to the client.
 * 
 * @author Mark Brightwell
 *
 */
public interface AuthorizationDetails extends Serializable {
  
  /**
   * Decode a Json string into the object.
   * @param json the Json string
   * @return the deserialized object
   */
  AuthorizationDetails fromJson(String json);

}
