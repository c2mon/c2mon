package cern.c2mon.shared.daq.command;



import org.apache.log4j.or.ObjectRenderer;


/**
 * Log4j ObjectRenderer for the SourceCommandTagValue class.
 *
 * The purpose of an object render is to log objects of a certain class
 * in a uniform way. 
 *
 * <PRE>
 * Logger log = Logger.getLogger( ... );
 * SourceCommandTagValue command =  ... ;
 * log.info(command);
 * ...
 * tag.log();
 * </PRE>
 *
 * The output in the log file will be (for example) : 
 * <PRE>
 * 
 * </PRE>
 */
 
public class SourceCommandTagValueRenderer implements ObjectRenderer {

  /**
   * Default constructor.
   */
  public SourceCommandTagValueRenderer() {/* Nothing to do */}

  /**
   * Implementation of the ObjectRenderer interface
   * @param o   the DataTagCacheObject to be rendered
   * @return    a string representation of the DataTagCacheObject, null if the
   * object passed as a parameter is null.
   */
  public String doRender(Object o) {
    if (o != null) {
  
      if (o instanceof SourceCommandTagValue) {
        SourceCommandTagValue command = (SourceCommandTagValue) o;
        StringBuffer str = new StringBuffer();
        str.append("COMMAND");
        str.append('\t');
        str.append(command.getId());
        str.append('\t');    
        str.append(command.getName());
        str.append('\t');
        //str.append(command.getMode());
        //str.append('\t');
        str.append(command.getDataType());
        str.append('\t');  
        str.append(command.getValue());
                              
        return str.toString(); 
      }
      else {
        // if someone passed an object other than SourceCommandTagValue
        return o.toString();
      }
    }
    else {
      // if somebody decided to pass a null parameter
      return null;        
    }
  }
}
