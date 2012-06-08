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

/** An exception representing a problem on the Server (service side).
 * 
 * @author felixehm 
 */
@SuppressWarnings("serial")
public class RemoteServerException extends ServiceException {

    public RemoteServerException(String msg) {
        super (msg);
    }

}
