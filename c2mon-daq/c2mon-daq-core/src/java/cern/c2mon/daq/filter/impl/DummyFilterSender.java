package cern.c2mon.daq.filter.impl;


import org.apache.log4j.Logger;

import cern.c2mon.daq.common.messaging.JmsLifecycle;
import cern.c2mon.daq.filter.IFilterMessageSender;
import cern.tim.shared.daq.filter.FilteredDataTagValue;

/**
 * Dummy implementation of the FilterMessageSender which can be plugged into the
 * application when no filtering is needed . (notice that filtering can also be
 * disabled using a command line parameter, but this is not necessary supported
 * by all implementations yet, so TODO : connect is not called in this case, but
 * disconnect and addValue still are, and this should not be the case so as to
 * be implementation independent...).
 * 
 * @author mbrightw
 * 
 */
public class DummyFilterSender implements IFilterMessageSender, JmsLifecycle {

   /*
    * The system logger.
    */
    private static final Logger LOGGER = Logger.getLogger("FilteredDataTagLogger");    
    
    @Override
    public void connect() {
        // TODO Auto-generated method stub

    }

    @Override
    public void shutdown() {
        // TODO Auto-generated method stub

    }

    @Override
    public void addValue(FilteredDataTagValue dataTagValue) {        
        LOGGER.info(dataTagValue);
    }

}
