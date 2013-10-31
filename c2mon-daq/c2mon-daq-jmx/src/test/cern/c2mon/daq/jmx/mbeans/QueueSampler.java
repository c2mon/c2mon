/**
 * Copyright (c) 2013 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.jmx.mbeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

public class QueueSampler extends NotificationBroadcasterSupport implements QueueSamplerMXBean {

    private long sequenceNumber = 1;

    private Queue<String> queue;

    public QueueSampler(Queue<String> queue) {
        this.queue = queue;
    }

    @Override
    public QueueSample getQueueSample() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("CATEGORY1/process1/metric1", 1000);
        map.put("CATEGORY1/process2/metric2", 2000);

        List<String> list = new ArrayList<String>();
        list.add("str1");
        list.add("str2");
        list.add("str3");

        Set<Integer> set = new HashSet<Integer>();
        set.add(1);
        set.add(2);
        set.add(3);

        synchronized (queue) {
            return new QueueSample(System.currentTimeMillis(), queue.size(), queue.peek(), map, list, set);
        }
    }

    public void clearQueue() {
        synchronized (queue) {
            queue.clear();
        }
    }

    @Override
    public void updateMetricInsideMap(Integer newValue) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("CATEGORY1/process1/metric1", 1000);
        map.put("CATEGORY1/process2/metric2", newValue);

        List<String> list = new ArrayList<String>();
        list.add("str1");
        list.add("str2");
        list.add("str3");

        Set<Integer> set = new HashSet<Integer>();
        set.add(1);
        set.add(2);
        set.add(3);

        QueueSample qs = null;
        synchronized (queue) {
            qs = new QueueSample(System.currentTimeMillis(), queue.size(), queue.peek(), map, list, set);
        }

        Notification n = new AttributeChangeNotification(this, sequenceNumber++, System.currentTimeMillis(),
                "QueueSample changed", "QueueSample", "CompositeData", null, qs);

        sendNotification(n);
    }
}