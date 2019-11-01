/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.test.cache;

import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.command.CommandExecutionDetails;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl;

import java.sql.Timestamp;

public class CommandTagCacheObjectFactory extends AbstractCacheObjectFactory<CommandTagCacheObject> {

  @Override
  public CommandTagCacheObject sampleBase() {
    CommandTagCacheObject commandTag =
      new CommandTagCacheObject(2000L, "Test command tag",
        "Test command tag desc", "Float", DataTagConstants.MODE_OPERATIONAL);
    commandTag.setEquipmentId(100L);
    commandTag.setMaximum(5f);
    commandTag.setMinimum(1f);

    commandTag.setAuthorizationDetails(createRbacAuthorizationDetails());
    commandTag.setClientTimeout(10000);
    try {
      commandTag.setHardwareAddress(new OPCHardwareAddressImpl("test"));
    } catch (ConfigurationException e) {
      e.printStackTrace();
    }
    commandTag.setSourceTimeout(10000);
    commandTag.setSourceRetries(4);
    commandTag.setExecTimeout(10000);

    //set process field - usually loaded using join from DB -  here must set to parent of Eq 300000
    commandTag.setProcessId(90L);

    CommandExecutionDetails<Long> commandExecutionDetails = new CommandExecutionDetails<>();
    commandExecutionDetails.setExecutionStartTime(new Timestamp(System.currentTimeMillis() - 1000));
    commandExecutionDetails.setExecutionEndTime(new Timestamp(System.currentTimeMillis()));
    commandExecutionDetails.setValue(10L);
    commandTag.setCommandExecutionDetails(commandExecutionDetails);
    return commandTag;
  }

  private RbacAuthorizationDetails createRbacAuthorizationDetails() {
    RbacAuthorizationDetails details = new RbacAuthorizationDetails();
    details.setRbacClass("class");
    details.setRbacDevice("device");
    details.setRbacProperty("property");
    return details;
  }
}
