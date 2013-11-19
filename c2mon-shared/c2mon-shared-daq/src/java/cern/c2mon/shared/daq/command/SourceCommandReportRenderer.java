package cern.c2mon.shared.daq.command;

import org.apache.log4j.or.ObjectRenderer;


/**
 * Log4j ObjectRenderer for the SourceCommandTagReport class.
 *
 * The purpose of an object render is to log objects of a certain class
 * in a uniform way. 
 *
 * <PRE>
 * Logger log = Logger.getLogger( ... );
 * CommandReport rep =  ... ;
 * log.info(rep);
 * ...
 * tag.log();
 * </PRE>
 *
 * The output in the log file will be (for example) : 
 * <PRE>
 * 
 * </PRE>
 */
 
public class SourceCommandReportRenderer implements ObjectRenderer {

  /**
   * Default constructor.
   */
  public SourceCommandReportRenderer() {/* Nothing to do */}

  /**
   * Implementation of the ObjectRenderer interface
   * @param o   the DataTagCacheObject to be rendered
   * @return    a string representation of the DataTagCacheObject, null if the
   * object passed as a parameter is null.
   */
  public String doRender(Object o) {
    if (o != null) {
      if (o instanceof SourceCommandTagReport) {
        SourceCommandTagReport rep = (SourceCommandTagReport) o;
        StringBuffer str = new StringBuffer();
        str.append("REPORT");
        str.append('\t');
        str.append(rep.getId());
        str.append('\t');
        str.append(rep.getName());
        str.append('\t');
        str.append(rep.getStatus());
        str.append('\t');
        str.append(rep.getFullDescription());
        str.append('\t');
        str.append(rep.getReturnValue());
        str.append('\t');                      
        return str.toString(); 
      }       
      else {
        // if someone passed an object other than CommandReport
        return o.toString();
      }  
    } 
    else {
      // if somebody decided to pass a null parameter
      return null;      
    }
  }
}
