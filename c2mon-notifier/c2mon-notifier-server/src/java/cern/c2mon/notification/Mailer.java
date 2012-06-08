/**
 * 
 */
package cern.c2mon.notification;

/**
 * @author felixehm
 *
 */
public interface Mailer {

	public void sendEmail(String to, String subject, String content) throws Exception;
	
}
