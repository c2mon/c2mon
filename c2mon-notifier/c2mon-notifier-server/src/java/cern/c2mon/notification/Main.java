/**
 * 
 */
package cern.c2mon.notification;

import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * @author felixehm
 *
 */
public class Main {

	public static void main(String[] args) {
			System.setProperty("log4j.configuration", System.getProperty(
					"log4j.configuration",
					"file:log4j.properties"));
			
			FileSystemXmlApplicationContext context = null;
			try {
				context = new FileSystemXmlApplicationContext(
					System.getProperty("server-context", "classpath:cern/c2mon/notification/context.xml"));
			// wait until we close, die or whatever
				while (context.isRunning()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch(Exception ex) {
				ex.printStackTrace();
				System.exit(1);
			}
			
	}

}
