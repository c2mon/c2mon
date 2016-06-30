/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.shared.daq.command;

import cern.c2mon.shared.daq.messaging.DAQResponse;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The SourceCommandTagReport is a data transfer object. After each failed or
 * successful command execution, a DAQ process creates a SourceCommandTagReport
 * and sends it to the server (as an XML message using the object's toXML()
 * method). The server reads the object back from the XML message and sends an
 * appropriate command report back to the client.
 *
 * @author Jan Stowisek
 * @version $Revision: 1.15 $ ($Date: 2006/05/15 15:43:13 $ - $State: Exp $)
 */
@Data
public final class SourceCommandTagReport implements DAQResponse {

    public enum Status{

    /**
     * Command report status indicating that the command was successfully
     * executed.
     */
    STATUS_OK("OK"),

    /**
     * Command report status indicating that the command could not be executed.
     */
    STATUS_NOK_FROM_EQUIPMENTD("Execution failed"),

    /**
     * Command report status indicating that the command was not executed
     * because the specified equipment-id was unknown to the DAQ process. This
     * status hints at an error in the DAQ process configuration.
     */
    STATUS_NOK_INVALID_EQUIPMENT("Execution failed: equipment unknown to the DAQ process"),

    /**
     * Command report status indicating that the command was not executed
     * because of an conversion error.
     */
    STATUS_NOK_CONVERSION_ERROR("Execution failed: cannot convert TIM data type to source data type"),

    /**
     * Command report status indicating that the command test was successful.
     * This status will only be returned for commands in test mode, which are
     * not actually sent to the hardware.
     */
    STATUS_TEST_OK("Test OK"),

    /**
     * Command report status indicating that the command was not executed
     * because the specified command-id was unknown to the DAQ process. This
     * status hints at an error in the DAQ process configuration.
     */
    STATUS_NOK_INVALID_COMMAND("Execution failed: command unknown to the DAQ process"),

    /**
     * Command report status indicating that the command was (most probably) not
     * executed because the source timeout was exceeded.
     */
    STATUS_NOK_TIMEOUT("Execution failed: command execution timed out");

        String description;

        Status(String description){
            this.description = description;
        }
    }
    /**
     * The id of this report
     */
    protected Long id;

    protected String name;

    /**
     * Command execution status.
     */
    protected Status status;

    /**
     * Additional free-text information about the execution status, if
     * available.
     */
    protected String description;

    /**
     * Time of command execution/report creation.
     */
    protected long timestamp;

    /**
     * Optional return value from the equipment after successful command execution
     */
    private String returnValue;

    /**
     * Log4j Logger for logging DataTag values.
     */
    protected static final Logger cmdlog = LoggerFactory.getLogger("SourceCommandTagLogger");

    /**
     * Default Constructor. Creates a SourceCommandReport object indicating
     * successful command execution.
     */
    public SourceCommandTagReport() {
        // only used internally by the fromXML method;
    }

    /**
     * Constructor.
     *
     * @param pStatus
     *            execution status
     * @param pStatusDesc
     *            additional free-text information about the status.
     */
    public SourceCommandTagReport(final Status pStatus, final String pStatusDesc) {
        this(pStatus, pStatusDesc, System.currentTimeMillis());
    }

    /**
     * Constructor.
     *
     * @param pStatus
     *            execution status
     * @param pStatusDesc
     *            additional free-text information about the status.
     */
    public SourceCommandTagReport(Long pId, String pName, final Status pStatus, final String pStatusDesc) {
        this(pId, pName, pStatus, pStatusDesc, System.currentTimeMillis());
    }

    /**
     * Constructor.
     *
     * @param pStatus
     *            execution status
     * @param pStatusDesc
     *            additional free-text information about the status.
     * @param pTimestamp
     *            time in milliseconds
     */
    public SourceCommandTagReport(final Status pStatus, final String pStatusDesc, final long pTimestamp) {
        this(null, null, pStatus, pStatusDesc, pTimestamp);
    }

    /**
     * Constructor.
     *
     * @param pId
     *            - command's id
     * @param pName
     *            - command's name
     * @param pStatus
     *            execution status
     * @param pStatusDesc
     *            additional free-text information about the status.
     * @param pTimestamp
     *            time in milliseconds
     */
    public SourceCommandTagReport(final Long pId, final String pName, final Status pStatus, final String pStatusDesc, final long pTimestamp) {
        this(pId, pName, pStatus, pStatusDesc, null, pTimestamp);
    }

    public SourceCommandTagReport(Long id, String name, Status cmdExecutionStatus, String cmdExecutionDescription, String returnValue , final long timestamp) {
        this.id = id;
        this.name = name;
        this.status = cmdExecutionStatus;
        this.description = cmdExecutionDescription;
        this.timestamp = timestamp;
        this.returnValue = returnValue;
    }

//    /**
//     * Get an XML representation of this object.
//     *
//     * @return an XML representation of this object
//     */
//    public String toXML() {
//        // Initialise StringBuffer with approximate message size (without
//        // description)
//        StringBuffer str = new StringBuffer(105);
//        str.append("<?xml version=\"1.0\"?>\n");
//        str.append("<SourceCommandReport>\n");
//        str.append("<status>");
//        str.append(this.status);
//        str.append("</status>\n");
//        if (this.statusDesc != null) {
//            str.append("<description><![CDATA[");
//            str.append(this.statusDesc);
//            str.append("]]></description>\n");
//        }
//        if (getReturnValue() != null) {
//            str.append("<return-value><![CDATA[");
//            str.append(getReturnValue());
//            str.append("]]></return-value>\n");
//        }
//        str.append("<timestamp>");
//        str.append(this.timestamp);
//        str.append("</timestamp>\n");
//
//        str.append("</SourceCommandReport>\n");
//        return str.toString();
//    }

    /**
     * Get a complete textual description of the command execution status. The
     * complete description contains the predefined description of the execution
     * status as well as an optional user-defined part.
     *
     * @return a complete textual description of the command execution status.
     */
    public String getFullDescription() {
        if (this.description != null) {
            return status.description + " : " + this.description;
        } else {
            return status.description;
        }
    }

//    /**
//     * Create a SourceCommandTagReport object from an XML element.
//     *
//     * @param pElement
//     *            Root element of the XML document representing the
//     *            SourceCommandTagReport object.
//     */
//    public static SourceCommandTagReport fromXML(final Element pElement) {
//        SourceCommandTagReport result = null;
//
//        if (pElement.getTagName().equals("SourceCommandReport")) {
//            result = new SourceCommandTagReport();
//
//            NodeList fields = pElement.getChildNodes();
//            String fieldName;
//            String fieldValueString;
//            Node fieldNode;
//            int fieldsCount = fields.getLength();
//
//            for (int i = 0; i != fieldsCount; i++) {
//                fieldNode = fields.item(i);
//                if (fieldNode.getNodeType() == Node.ELEMENT_NODE) {
//                    fieldName = fieldNode.getNodeName();
//                    fieldValueString = fieldNode.getTextContent();
//                    if (fieldName.equals("status")) {
//                        result.status = Integer.parseInt(fieldValueString);
//                    } else if (fieldName.equals("description")) {
//                        result.statusDesc = fieldValueString;
//                    } else if (fieldName.equals("return-value")) {
//                        result.setReturnValue(fieldValueString);
//                    } else if (fieldName.equals("timestamp")) {
//                        try {
//                            result.timestamp = Long.parseLong(fieldValueString);
//                        } catch (NumberFormatException nfe) {
//                            result.timestamp = System.currentTimeMillis();
//                        }
//                    }
//                }
//            }
//        }
//        return result;
//    }

    public void log() {
        cmdlog.info(this.toString());
    }

    @Override
    public String toString() {
        return "REPORT" + '\t' + this.getId() + '\t' + this.getName() + '\t' + this.getStatus() + '\t' + this.getFullDescription() + '\t' + this
            .getReturnValue() + '\t';
    }
}
