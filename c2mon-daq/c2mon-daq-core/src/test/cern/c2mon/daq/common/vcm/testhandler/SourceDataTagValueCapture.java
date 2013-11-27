package cern.c2mon.daq.common.vcm.testhandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.Capture;

import cern.c2mon.shared.daq.datatag.SourceDataTagValue;

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
