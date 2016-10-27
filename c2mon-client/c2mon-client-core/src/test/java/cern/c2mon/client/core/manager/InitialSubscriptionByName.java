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

public class InitialSubscriptionByName {
  
  public InitialSubscriptionByName() {
    C2monServiceGateway.startC2monClientSynchronous();
    TagService tagService = C2monServiceGateway.getTagService();
    
    Set<String> tagIds = new HashSet<>();
    tagIds.add("CS.L05.CMS-DSS:PRESSURE_PM54");
    tagIds.add("YA.L05.PMIL.5411=R542:DR");
    tagIds.add("FU.L08.P$MSW01-3876_PMWTB01:MESURE");
    tagIds.add("FU.L06.P$MSW01-3676_PMWTB01:MESURE");
    tagIds.add("YA.L05.PATL.5611=UJ56:DR");
    
        
    tagService.subscribeByName(tagIds, new Listener());
    try {
      Thread.sleep(10000);
    }
    catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("\nRegistering second listener");
    tagIds.add("YA.L05.PMIL.5411=R542:ALARM");
    tagService.subscribeByName(tagIds, new Listener());
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
    new InitialSubscriptionByName();
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
