package cern.c2mon.server.client.request;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

/**
 * This class is responsible for logging all uncatched
 * exceptions of the <code>TagRequestHandler</code>.
 *
 * @author Matthias Braeger
 */
@Service("tagRequestErrorHandler")
public class TagRequestErrorHandler implements ErrorHandler {

  /** Log4j logger */
  private static final Logger LOG = Logger.getLogger(TagRequestErrorHandler.class);
  
  @Override
  public void handleError(final Throwable exception) {
    LOG.error("A problem occured while handling a tag request.", exception);
  }

}
