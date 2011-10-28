package cern.tim.driver.test
import org.apache.log4j.Logger

/**
 * Trait to add a logger to every class which uses their class name as logger name.
 */
trait LogHelper {
  
  lazy val logger = Logger.getLogger(this.getClass());  
  
}