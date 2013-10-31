/**
 * Copyright (c) 2013 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.jmx.mbeans;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MXBean;

@MXBean
public class QueueSample implements Serializable {

    private static final long serialVersionUID = 6083179971270376025L;
    
    private final long timestamp;
    private final int size;
    private final String head;
    private final Map<String, Integer> map;
    private List<String> list;
    private Set<Integer> set;

    @ConstructorProperties({ "timestamp", "size", "head", "map", "list", "set" })
    public QueueSample(long timestamp, int size, String head, Map<String, Integer> map, List<String> list,
            Set<Integer> set) {
        this.timestamp = timestamp;
        this.size = size;
        this.head = head;
        this.map = map;
        this.list = list;
        this.set = set;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getSize() {
        return size;
    }

    public String getHead() {
        return head;
    }

    public Map<String, Integer> getMap() {
        return map;
    }

    public List<String> getList() {
        return list;
    }

    public Set<Integer> getSet() {
        return set;
    }
}