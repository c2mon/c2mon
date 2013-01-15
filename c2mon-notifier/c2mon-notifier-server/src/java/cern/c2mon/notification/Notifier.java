/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.notification;

public interface Notifier {

    
    public void sendInitialReport(Tag update);
    
    public void sendReportOnRuleChange(Tag update);
    
    public void sendReportOnValueChange(Tag udpate);
    
    public void sendSourceAvailabilityReport(Tag update);
}
