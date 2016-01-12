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

package cern.c2mon.daq.common.vcm.testhandler;

import cern.c2mon.shared.common.datatag.ISourceDataTag;

/**
 * The<code>SenderTask</code> class implements a runnable responsible for sending periodic updates. It emulates the
 * reception of real information from a datasource. Together with the <code>EspMessageHandler</code> class it is used in
 * order to test VCMs.
 * 
 * @author wbuczak
 */
public class SenderTask implements Runnable {

    EspMessageHandler handler;

    ISourceDataTag tag;

    int counter = 0;

    public SenderTask(EspMessageHandler handler, ISourceDataTag tag) {
        this.handler = handler;
        this.tag = tag;
    }

    @Override
    public void run() {
        if (!handler.skipUpdate(tag.getId())) {

            String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(System
                    .currentTimeMillis()));

            System.out.println(String.format("[%s] sending event (#%d), counter=%d", timestamp, tag.getId(), counter));
            handler.getEquipmentMessageSender().sendTagFiltered(tag, counter, System.currentTimeMillis(), null);

        }

        counter += handler.getStep(tag.getId());
    }

}
