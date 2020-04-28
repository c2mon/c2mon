/******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.shared.util.buffer;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SynchroBufferQueueTest {
  SynchroBufferQueue<Integer> queue;
  int size = 100_000;
  
  @Before
  public void before() {
    queue = new SynchroBufferQueue<>();
    int count = 1;
    while (count <= size) {
      Assert.assertTrue(queue.add(count++));
    }
  }

  @Test
  public void testPollWithMaxSize() throws InterruptedException {
    long timeout = 500L;
    long start = System.currentTimeMillis();
    List<Integer> list = queue.poll(timeout, size);
    long end = System.currentTimeMillis();
    log.debug("Poll took {} ms", end - start);
    Assert.assertTrue(end - start < timeout);
    
    Assert.assertEquals(list.size(), size);
    int count = 1;
    for (Integer element : list) {
      Assert.assertEquals(element, Integer.valueOf(count++));
    }
    Assert.assertTrue(queue.isEmpty());
  }
  
  @Test
  public void testPollWithSmallBuffer() throws InterruptedException {
    int count = 1;
    long timeout = 500L;
    List<Integer> list;
    int loopCount = 0;
    int loops = 10;
    
    while (queue.size() > 0) {
      loopCount++;
      long start = System.currentTimeMillis();
      list = queue.poll(timeout, size/loops);
      long end = System.currentTimeMillis();
      log.debug("Poll took {} ms", end - start);
      Assert.assertTrue(end - start < timeout);
      
      Assert.assertEquals(list.size(), size/loops);
      
      for (Integer element : list) {
        Assert.assertEquals(element, Integer.valueOf(count++));
      }
    }
    
    Assert.assertEquals(size, count - 1);
    Assert.assertEquals(loopCount, loops);
    Assert.assertTrue(queue.isEmpty());
  }
  
  @Test
  public void testPollWithBigBuffer() throws InterruptedException {
    long timeout = 200L;
    
    long start = System.currentTimeMillis();
    List<Integer> list = queue.poll(timeout, size*2);
    long end = System.currentTimeMillis();
    log.debug("Poll took {} ms", end - start);
    Assert.assertTrue(end - start >= timeout);
    
    Assert.assertEquals(list.size(), size);
    
    int count = 1;
    for (Integer element : list) {
      Assert.assertEquals(element, Integer.valueOf(count++));
    }
    
    Assert.assertEquals(size, count - 1);
    Assert.assertTrue(queue.isEmpty());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testPollWithWrongParameters() throws InterruptedException {
    List<Integer> list; 
    
    list = queue.poll(0L, size);
    Assert.assertEquals(list.size(), 0);
    
    list = queue.poll(-10L, size);
    Assert.assertEquals(list.size(), 0);
    
    list = queue.poll(100L, 0);
    Assert.assertEquals(list.size(), 0);
    
    // throws IllegalArgumentException
    list = queue.poll(100L, -111);
  }
}
