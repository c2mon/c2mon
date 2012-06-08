/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.notification.shared;

/** An exception which occurs while using the notification service API.
 * 
 * @author felixehm 
 */
public class ServiceException extends RuntimeException {

    public ServiceException(String msg) {
        super(msg);
    }
    
    public ServiceException(Exception ex) {
        super(ex);
    }
}
