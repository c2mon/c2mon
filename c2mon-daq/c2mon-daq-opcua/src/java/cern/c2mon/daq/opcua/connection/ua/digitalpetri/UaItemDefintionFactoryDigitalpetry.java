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
package cern.c2mon.daq.opcua.connection.ua.digitalpetri;

import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;

import cern.c2mon.daq.opcua.connection.common.IItemDefinitionFactory;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.OPCHardwareAddress;
import cern.c2mon.shared.common.datatag.address.OPCHardwareAddress.ADDRESS_TYPE;

/**
 * ItemDefinitionFactory implementation for the UA endpoint.
 *
 * @author Andreas Lang
 *
 */
public class UaItemDefintionFactoryDigitalpetry
        implements IItemDefinitionFactory<UAItemDefintionDigitalpetri> {

    /**
     * Creates a new ItemDefintion.
     *
     * @param id The id of the new item definition.
     * @param hardwareAddress The hardware address to create the item
     * definition from.
     * @return The new UA item definition.
     */
    @Override
    public UAItemDefintionDigitalpetri createItemDefinition(
            final long id, final HardwareAddress hardwareAddress) {
        UAItemDefintionDigitalpetri definition;
        if (hardwareAddress instanceof OPCHardwareAddress) {
            OPCHardwareAddress opcHardwareAddress =
                (OPCHardwareAddress) hardwareAddress;
            if (opcHardwareAddress.getAddressType() != null) {
                // default namespace if not specified
                int namespace = 2;
                if (opcHardwareAddress.getNamespaceId() != 0)
                    namespace = opcHardwareAddress.getNamespaceId();
                String opcItemName = opcHardwareAddress.getOPCItemName();
                String redundantOPCItemName =
                    opcHardwareAddress.getOpcRedundantItemName();
                NodeId nodeId = new NodeId(namespace, opcItemName);
                if (redundantOPCItemName != null
                        && !redundantOPCItemName.trim().equals("")) {
                    NodeId redundantNodeId = createNodeId(
                            namespace, redundantOPCItemName,
                            opcHardwareAddress.getAddressType());
                    definition = new UAItemDefintionDigitalpetri(id, nodeId, redundantNodeId);
                }
                else {
                    definition = new UAItemDefintionDigitalpetri(id, nodeId);
                }
            }
            else {
                definition = null;
            }
        }
        else {
            definition = null;
        }
        return definition;
    }

    /**
     * Creates the node id of the correct type.
     *
     * @param namespace The namespace of the node.
     * @param address The address string of the node.
     * @param addressType The address type of the node.
     * @return The new NodeId.
     */
    private NodeId createNodeId(final int namespace,
            final String address, final ADDRESS_TYPE addressType) {
        NodeId nodeId;
        switch (addressType) {
        case STRING:
            String uaAddress = UAAddressTransformer.transform(address);
            nodeId = new NodeId(namespace, uaAddress);
            break;
        case GUID:
//            UUID uuid = UUID.fromString(address);
//            nodeId = new NodeId(namespace, uuid);
//            break;
        case NUMERIC:
//            nodeId = new NodeId(
//                    namespace, uint(address));
//            break;
        default:
            // should not happen
            nodeId = null;
            break;
        }
        return nodeId;
    }

}
