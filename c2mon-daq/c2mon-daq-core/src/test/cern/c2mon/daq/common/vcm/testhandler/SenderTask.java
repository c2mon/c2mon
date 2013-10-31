/**
 * Copyright (c) 2013 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.common.vcm.testhandler;

import cern.tim.shared.daq.datatag.ISourceDataTag;

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
