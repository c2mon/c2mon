package cern.c2mon.client.core.manager;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.C2monServiceGateway;

class Test {
  
  
  
  public static void main(String[] args) {
    Tag tag = C2monServiceGateway.getTagService().get(14234234L);
    System.out.println("existing=" + tag.getDataTagQuality().isExistingTag());
  }
}