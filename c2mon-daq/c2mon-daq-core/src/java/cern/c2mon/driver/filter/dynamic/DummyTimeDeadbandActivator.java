/**
 * Copyright (c) 2013 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.driver.filter.dynamic;

import java.util.Map;

import cern.tim.shared.daq.datatag.SourceDataTag;

/**
 * The <code>DummyTimeDeadbandActivator</code> is used to satisfy spring dependencies for DAQs which are not using time
 * deadband filtering
 * 
 * @author wbuczak
 */
public class DummyTimeDeadbandActivator implements IDynamicTimeDeadbandFilterActivator {

    @Override
    public void newTagValueSent(long tagID) {

    }

    @Override
    public Map<Long, SourceDataTag> getDataTagMap() {

        return null;
    }

    @Override
    public void addDataTag(SourceDataTag sourceDataTag) {

    }

    @Override
    public void removeDataTag(SourceDataTag sourceDataTag) {

    }

    @Override
    public void clearDataTags() {

    }

}
