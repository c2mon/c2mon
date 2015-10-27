package cern.c2mon.server.client.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

/**
 * This class is responsible for logging all uncatched
 * exceptions of the <code>TagRequestHandler</code>.
 *
 * @author Matthias Braeger
 */
@Service("tagRequestErrorHandler")
public class ClientRequestErrorHandler implements ErrorHandler {

  /** Log4j logger */
  private static final Logger LOG = LoggerFactory.getLogger(ClientRequestErrorHandler.class);
  
  @Override
  public void handleError(final Throwable exception) {
    LOG.error("A problem occured while handling a tag request.", exception);
  }

}
