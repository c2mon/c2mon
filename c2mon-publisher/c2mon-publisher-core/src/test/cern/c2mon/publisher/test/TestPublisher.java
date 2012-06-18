package cern.c2mon.publisher.test;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.publisher.Publisher;
import cern.c2mon.shared.client.tag.TagConfig;

@Service
public class TestPublisher implements Publisher {

  /** LOG4J logger instance */
  private static final Logger LOG = Logger.getLogger(TestPublisher.class);
  
  /** tag update counter */
  private static int counter = 0;
  
  @Override
  public void onUpdate(final ClientDataTagValue cdt, final TagConfig cdtConfig) {
    LOG.debug("Got update for tag " + cdt.getId());
    counter++;
  }

  @Override
  public void shutdown() {
    // Do nothing
  }
  
  /**
   * @return The number of update calls received
   */
  public static int getUpdateCounter() {
    return counter;
  }

}
