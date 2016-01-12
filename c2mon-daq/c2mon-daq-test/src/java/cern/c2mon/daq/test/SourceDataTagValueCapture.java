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
package cern.c2mon.daq.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.Capture;

import cern.c2mon.shared.common.datatag.SourceDataTagValue;

/**
 * This toolkit class extends JUnit Capture class providing a mechanism to store multiple objects
 * of captured SourceDataTagValues
 * @author wbuczak
 */
public class SourceDataTagValueCapture extends Capture<SourceDataTagValue> {

    private static final long serialVersionUID = 5078553117185511145L;

    Map<Long, List<SourceDataTagValue>> vals = new HashMap<Long, List<SourceDataTagValue>>();

    @Override
    public void setValue(SourceDataTagValue value) {
        if (null == vals.get(value.getId()))
            vals.put(value.getId(), new ArrayList<SourceDataTagValue>());

        vals.get(value.getId()).add(value);
        super.setValue(value);
    }

    public SourceDataTagValue getFirstValue(final long tagId) {
        return vals.get(tagId).get(0);
    }

    public SourceDataTagValue getLastValue(final long tagId) {
        return vals.get(tagId).get(vals.get(tagId).size() - 1);
    }

    public SourceDataTagValue getValueAt(int index, final long tagId) {
        return vals.get(tagId).get(index);
    }

    public int getNumberOfCapturedValues(final long tagId) {
        return vals.get(tagId) == null ? 0 : vals.get(tagId).size();
    }
}
