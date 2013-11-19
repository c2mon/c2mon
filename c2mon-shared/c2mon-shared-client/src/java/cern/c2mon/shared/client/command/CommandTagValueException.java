package cern.c2mon.shared.client.command;


/**
 * @author Jan Stowisek
 * @version $Revision: 1.2 $ ($Date: 2004/10/28 16:07:06 $ - $State: Exp $)
 */

public class CommandTagValueException extends Exception {

  public CommandTagValueException() {
    super("Invalid command value.");
  }

  public CommandTagValueException(String message) {
    super(message);
  }
}
