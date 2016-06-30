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
package cern.c2mon.shared.common.command;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddressFactory;
import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Jan Stowisek
 * @version $Revision: 1.8 $ ($Date: 2005/02/01 17:04:58 $ - $State: Exp $)
 */
@Data
public class SourceCommandTag implements Cloneable, ISourceCommandTag {

    /**
     * Unique numeric identifier of the command tag
     */
    @Attribute
    private Long id;

    /**
     * Unique (human-readable) name of the command tag
     */
    @Attribute
    private String name;

    /**
     * HardwareAddress object used by the driver's EquipmentMessageHandler to
     * actually send the command value to the piece of equipment concerned.
     */
    @Element(name = "HardwareAddress")
    private HardwareAddress hardwareAddress;

    /**
     * Timeout in milliseconds. Depending on the way the equipment responds to
     * commands coming from TIM, this timeout has two different meanings:
     * <UL>
     * <LI>If the equipment sends an asynchronous response (e.g. a command
     * report) indicating whether command execution was successful or not, the
     * driver must wait no more than "sourceTimeout" milliseconds for such a
     * response. After this timeout, command execution shall be deemed to have
     * failed. Depending on the value of "sourceRetries", the driver might
     * immediately start another execution attempt or abandon execution.
     * <LI>If the equipment responds to command executions in a synchronous way,
     * the driver should wait no more than "sourceTimeout" milliseconds before
     * attempting to execute the command again (where appropriate). Ideally, the
     * driver should wait "sourceTimout" - "time to execute a command"
     * milliseconds before trying again.
     * </UL>
     */
    @Element(name = "source-timeout")
    private int sourceTimeout;

    /**
     * Maximum number of retries if an attempt to execute a command fails.
     */
    @Element(name = "source-retries")
    private int sourceRetries;

    /**
     * Creates a new SourceCommandTag with the provided name and id.
     * @param id The id of the new SourceCommandTag.
     * @param name The name of the new SourceCommandTag.
     */
    public SourceCommandTag(final Long id, final String name) {
        this(id, name, 0, 0, null);
    }

    /**
     * Creates a new SourceCommandTag.
     *
     * @param id The id of the new tag.
     * @param name The name of the new tag.
     * @param sourceTimeout The timeout till the command is considered as failed.
     * @param sourceRetries The number of retries which will be attempted if a command
     * fails.
     * @param hwAddress The hardware address of the command tag.
     */
    public SourceCommandTag(final Long id, final String name,
            final int sourceTimeout, final int sourceRetries,
            final HardwareAddress hwAddress) {
        this.id = id;
        this.name = name;
        this.sourceTimeout = sourceTimeout;
        this.sourceRetries = sourceRetries;
        this.hardwareAddress = hwAddress;
    }

    /**
     * No-arg constructor (required for JSON deserialisation).
     */
    public SourceCommandTag() {
    }

    /**
     * Generates a XML String from the provided command tag.
     *
     * @param cmd The command tag which should be used.
     * @return The XML String matching this command tag.
     */
    public static String toConfigXML(final CommandTag cmd) {
        StringBuffer str = new StringBuffer("    <CommandTag id=\"");

        str.append(cmd.getId());
        str.append("\" name=\"");
        str.append(cmd.getName());
        str.append("\">\n");

        str.append("      <source-timeout>");
        str.append(cmd.getSourceTimeout());
        str.append("</source-timeout>\n");

        str.append("      <source-retries>");
        str.append(cmd.getSourceRetries());
        str.append("</source-retries>\n");

        if (cmd.getHardwareAddress() != null) {
            str.append(cmd.getHardwareAddress().toConfigXML());
        }

        str.append("    </CommandTag>\n");
        return str.toString();
    }

    /**
     * Creates a SourceCommandTag from a DOM element. The provided element
     * MUST be a element CommandTag element.
     *
     * @param domElement The DOM element to use.
     * @return The created SourceCommandTag.
     */
    public static SourceCommandTag fromConfigXML(final org.w3c.dom.Element domElement) {
        Long id = Long.valueOf(domElement.getAttribute("id"));
        String name = domElement.getAttribute("name");

        SourceCommandTag result = new SourceCommandTag(id, name);

        Node fieldNode = null;
        String fieldName = null;
        String fieldValueString = null;
        NodeList fields = domElement.getChildNodes();

        int fieldsCount = fields.getLength();

        for (int i = 0; i < fieldsCount; i++) {
            fieldNode = fields.item(i);

            if (fieldNode.getNodeType() == 1) {
                // extract name of the XML node
                fieldName = fieldNode.getNodeName();
                // extract contents of the XML node
                fieldValueString = fieldNode.getFirstChild().getNodeValue();

                if (fieldName.equals("source-timeout")) {
                    result.sourceTimeout = Integer.parseInt(fieldValueString);
                }

                if (fieldName.equals("source-retries")) {
                    result.sourceRetries = Integer.parseInt(fieldValueString);
                }

                if (fieldName.equals("HardwareAddress")) {
                    result.hardwareAddress = HardwareAddressFactory.getInstance().fromConfigXML((org.w3c.dom.Element) fieldNode);
                }
            }
        }
        return result;
    }

    /**
     * Creates a deep clone of this object which means it also clones
     * the containing hardware address.
     *
     * @return The clone of the object.
     * @throws CloneNotSupportedException This exception should never happen with
     * a SourceCommandTag.
     */
    @Override
    public SourceCommandTag clone() throws CloneNotSupportedException {
        SourceCommandTag clonedSourceCommandTag = (SourceCommandTag) super.clone();
        HardwareAddress hardwareAddress = getHardwareAddress();
        if (hardwareAddress != null)
            clonedSourceCommandTag.setHardwareAddress(getHardwareAddress().clone());
        return clonedSourceCommandTag;
    }

    /**
     * Validates the command tag and throws an exception if validation
     * fails.
     *
     * @throws ConfigurationException Thrown if the command tag is not valid.
     */
    public void validate() throws ConfigurationException {
        if (hardwareAddress == null) {
            throw new ConfigurationException(
                    ConfigurationException.INVALID_PARAMETER_VALUE,
                    "Hardware address null. Command Tag not valid."
                    );
        }
        hardwareAddress.validate();
    }
}
