package cern.c2mon.web.configviewer.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;

public class CustomAuthListener implements ApplicationListener<AbstractAuthenticationEvent> {
 
 private static final Log logger = LogFactory.getLog(CustomAuthListener.class);

 @Override
 public void onApplicationEvent(AbstractAuthenticationEvent event) {
  
 final StringBuilder builder = new StringBuilder();
        builder.append("Authentication event ");
        builder.append(event.getClass().getSimpleName());
        builder.append(": ");
        builder.append(event.getAuthentication().getName());
        builder.append("; details: ");
        builder.append(event.getAuthentication().getDetails());

        if (event instanceof AbstractAuthenticationFailureEvent) {
            builder.append("; exception: ");
            builder.append(((AbstractAuthenticationFailureEvent) event).getException().getMessage());
        }

        logger.warn(builder.toString());

 }

}
