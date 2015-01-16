package cern.c2mon.client.core.manager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import cern.c2mon.client.common.listener.DataTagListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.C2monTagManager;

public class InitialSubscriptionTest {
  
  public InitialSubscriptionTest() {
    C2monServiceGateway.startC2monClientSynchronous();
    C2monTagManager tagManager = C2monServiceGateway.getTagManager();
    
    Set<Long> tagIds = new HashSet<Long>();
    tagIds.add(165479L);
    tagIds.add(187252L);
    tagIds.add(188130L);
    tagIds.add(165471L);
    tagIds.add(187200L);
    
        
    tagManager.subscribeDataTags(tagIds, new Listener());
    try {
      Thread.sleep(10000);
    }
    catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("Registering second listener");
    tagIds.add(187201L);
    tagManager.subscribeDataTags(tagIds, new Listener());
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
  
  class Listener implements DataTagListener {
    
    @Override
    public void onUpdate(ClientDataTagValue cdt) {
      System.out.println(cdt.getId() + ": timestamp[" + cdt.getTimestamp() + "] value[" + cdt.getValue() + "]");
    }

    @Override
    public void onInitialUpdate(Collection<ClientDataTagValue> initialValues) {
      System.out.println("********* onInitialValues START **********");
      for (ClientDataTagValue cdt : initialValues) {
        System.out.println(cdt.getId() + ": timestamp[" + cdt.getTimestamp() + "] value[" + cdt.getValue() + "]");
      }
      System.out.println("********* onInitialValues END **********");
    }
    
  }
}
