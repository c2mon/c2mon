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
package cern.c2mon.client.core.manager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import cern.c2mon.client.common.listener.TagListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.service.TagService;

public class InitialSubscriptionTest {
  
  public InitialSubscriptionTest() {
    C2monServiceGateway.startC2monClientSynchronous();
    TagService tagManager = C2monServiceGateway.getTagService();
    
    Set<Long> tagIds = new HashSet<Long>();
    tagIds.add(165479L);
    tagIds.add(187252L);
    tagIds.add(188130L);
    tagIds.add(165471L);
    tagIds.add(187200L);
    
        
    tagManager.subscribe(tagIds, new Listener());
    try {
      Thread.sleep(10000);
    }
    catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("Registering second listener");
    tagIds.add(187201L);
    tagManager.subscribe(tagIds, new Listener());
    System.out.println("Registering second listener - DONE!");
    
    try {
      Thread.sleep(60000);
    }
    catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public static void main(String[] args) {
    new InitialSubscriptionTest();
  }
  
  class Listener implements TagListener {
    
    @Override
    public void onUpdate(Tag cdt) {
      System.out.println(cdt.getId() + ": name[" +  cdt.getName() + "] timestamp[" + cdt.getTimestamp() + "] value[" + cdt.getValue() + "]");
    }

    @Override
    public void onInitialUpdate(Collection<Tag> initialValues) {
      System.out.println("********* onInitialValues START **********");
      for (Tag cdt : initialValues) {
        System.out.println(cdt.getId() + ": name[" +  cdt.getName() + "] timestamp[" + cdt.getTimestamp() + "] value[" + cdt.getValue() + "]");
      }
      System.out.println("********* onInitialValues END **********");
    }
    
  }
}
