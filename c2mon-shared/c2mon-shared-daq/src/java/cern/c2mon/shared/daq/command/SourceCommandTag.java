/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.shared.daq.command;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddressFactory;

/**
 * @author Jan Stowisek
 * @version $Revision: 1.8 $ ($Date: 2005/02/01 17:04:58 $ - $State: Exp $)
 */

public class SourceCommandTag implements Cloneable, ISourceCommandTag {

    /**
     * Unique numeric identifier of the command tag
     */
    private Long id;

    /**
     * Unique (human-readable) name of the command tag
     */
    private String name;

    /**
     * HardwareAddress object used by the driver's EquipmentMessageHandler to
     * actually send the command value to the piece of equipment concerned.
     */
    private HardwareAddress hwAddress;

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
    private int sourceTimeout;

    /**
     * Maximum number of retries if an attempt to execute a command fails.
     */
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
        this.hwAddress = hwAddress;
    }

    /**
     * Get the unique numeric identifier of the command tag.
     * 
     * @return the unique numeric identifier of the command tag.
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Set the unique numeric identifier of the command tag.
     * 
     * @param pId
     *            the new unique numeric identifier for the command tag.
     */
    public void setId(final Long pId) {
        this.id = pId;
    }

    /**
     * Get the unique name of the command tag.
     * 
     * @return the unique name of the command tag.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the unique name of the command tag.
     * 
     * @param pName
     *            the new name for the command tag.
     */
    public void setName(final String pName) {
        this.name = pName;
    }

    /**
     * Gets the source timeout of this tag.
     * 
     * @return The source timeout.
     */
    @Override
    public int getSourceTimeout() {
        return this.sourceTimeout;
    }

    /**
     * Sets the source timeout of this tag.
     * 
     * @param newSourceTimeout The new source timeout.
     */
    public void setSourceTimeout(final int newSourceTimeout) {
        this.sourceTimeout = newSourceTimeout;
    }

    /**
     * Gets the maximum number of retries sending this command.
     * 
     * @return The maximum number of command execution retries.
     */
    public int getSourceRetries() {
        return this.sourceRetries;
    }

    /**
     * Sets the maximum number of command execution retries.
     * 
     * @param newSourceRetries The new maximum number of retries.
     */
    public void setSourceRetries(final int newSourceRetries) {
        this.sourceRetries = newSourceRetries;
    }

    /**
     * Returns the hardware address of this tag.
     * 
     * @return The hardware address of this tag.
     */
    public HardwareAddress getHardwareAddress() {
        return this.hwAddress;
    }

    /**
     * Sets the HardwareAddress of this command tag.
     * @param pHwAddress The hardware address of this command tag.
     */
    public void setHardwareAddress(final HardwareAddress pHwAddress) {
        this.hwAddress = pHwAddress;
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
    public static SourceCommandTag fromConfigXML(final Element domElement) {
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
                    result.hwAddress = HardwareAddressFactory.getInstance().fromConfigXML((Element) fieldNode);
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
        if (hwAddress == null) {
            throw new ConfigurationException(
                    ConfigurationException.INVALID_PARAMETER_VALUE, 
                    "Hardware address null. Command Tag not valid."
                    );
        }
        hwAddress.validate();
    }
}
