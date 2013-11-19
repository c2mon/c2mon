package cern.c2mon.shared.daq.datatag;


import javax.jms.Topic;
import org.w3c.dom.Element;

import org.apache.log4j.Logger;


//imported as in into TIM2
/**
 * This class represents a request to a DAQ process to send the 
 * last know values of a number of DataTags.
 * 
 * A request is generally created by the application server, typically 
 * by BigBrother (when an alive tag is received again after it had
 * expired). 
 * 
 * The DAQ needs to be able to handle three types of request:
 * <ul>
 * <li><b>DataTag</b>: request the value of an individual DataTag</li>
 * <li><b>Equipment</b>: request the values of all DataTags attached to an equipment</li> 
 * <li><b>Process</b>: request the values of all DataTags attached to a DAQ process</li>
 * </ul>
 * The DAQ process is expected to respond with a SourceDataTagValueResponse
 * sent to the server on the topic contained in the request.
 *   
 * @author stowisek
 *
 */

public class SourceDataTagValueRequest implements java.io.Serializable {

  private static final Logger LOG = Logger.getLogger(SourceDataTagValueRequest.class);

  /**
   * Identifier of the process/equipment/datatag for which the tag values are requested.
   */
  private Long id;
  
  /**
   * Request type: TYPE_PROCESS, TYPE_EQUIPMENT or TYPE_DATATAG
   */
  private String type;
  
  /**
   * Topic on which the DAQ is expected to publish the response to this 
   * request.
   */
  protected Topic replyTopic;
  
  // TODO use enum instead
  public static final String TYPE_PROCESS = "PROCESS";

  public static final String TYPE_EQUIPMENT = "EQUIPMENT";

  public static final String TYPE_DATATAG = "DATATAG";

  /**
   * Constructor
   * @param pType request type
   * @param pId identifier of the equipment/process/datatag for which the values are requested
   */
  public SourceDataTagValueRequest(final String pType, final Long pId) {
    this.id = pId;
    this.type = pType;
  }

  /**
   * Get the request type
   * @return the type of request
   */
  public String getType() {
    return this.type;
  }

  /**
   * Get the identifier of the Equipment/Process/DataTag for which the 
   * values are requested, depending on the request type.
   * @return
   */
  public Long getId() {
    return this.id;
  }
  
  public void setType(final String pType) {
    this.type = pType;
  }
  
  public void setId(final Long pId) {
    this.id = pId;
  }
  
  public void setReplyTopic(final Topic pReplyTopic) {
    this.replyTopic = pReplyTopic;
  }
  
  /**
   * Get the name of the JMS topic on which the DAQ is expected to
   * send its reply.
   */
  public Topic getReplyTopic() {
    return this.replyTopic;
  }
  
  
  public String toXML() {
    StringBuffer str = new StringBuffer(150);
    str.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    str.append("<DataTagValueUpdateRequest ");
    if (this.type != null) {
      str.append("type=\"");
      str.append(this.type);
      str.append("\" ");
    }
    if (this.id != null) {
      str.append("id=\"");
      str.append(this.id);
      str.append("\" ");
    }
    str.append("/>");
    return str.toString();
  }
  
  public synchronized static SourceDataTagValueRequest fromXML(Element domElement) {
    Long id = null;
    String type = null;
    String idStr = null;
    // extract attributes 
    try {
      idStr = domElement.getAttribute("id");
      if (idStr != null && (!idStr.equals(""))) {
        id = Long.valueOf(idStr);
      }
      type = domElement.getAttribute("type");
    } catch (NumberFormatException nfe) {
      LOG.error(nfe);
      return null;
    } catch (Exception ex) {
      LOG.error(ex);
      return null;
    }

    return new SourceDataTagValueRequest(type, id);
  }
  
  /**
   * Return an XML representation of the SourceDataTagValueRequest
   */
  public String toString() {
    return toXML();  
  }
  
  /**
   * Checks whether two request objects are equal by comparing their
   * members. Please note that a SourceDataTagValueRequest object created
   * using fromXML() may NOT be equals to the object creating the XML as
   * the replyTopic is also taken into account in the comparison. 
   */
  public boolean equals(final Object pObj) {
	  boolean result = pObj != null && pObj instanceof SourceDataTagValueRequest;
	  if (result) {
		SourceDataTagValueRequest copy = (SourceDataTagValueRequest) pObj;
		result = 
			((this.type == null) ? (copy.type == null) : (this.type.equals(copy.type)))
			&& ((this.id == null) ? (copy.id == null) : (this.id.equals(copy.id)))
			&& (this.replyTopic == null ? copy.replyTopic == null : this.replyTopic.equals(copy.replyTopic));
			
	  }
	  return result;
  }

}
