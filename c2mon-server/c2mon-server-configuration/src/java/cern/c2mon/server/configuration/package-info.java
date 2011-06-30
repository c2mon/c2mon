/**
 * Contains all the public interfaces of the C2MON configuration module. 
 * 
 * <p>The ConfigurationLoader bean is available for loading configurations into
 * the server cache (and DB). Configuration requests can also be sent to JMS
 * on the ${jms.config.destination} queue.
 * 
 * <p>IMPORTANT: no other interface or class should be used externally
 */
package cern.c2mon.server.configuration;