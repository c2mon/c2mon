package cern.c2mon.web.configviewer.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Helper class that contains methods used for debugging.
 */
public class DebugUtil {

  /**
   * Debug method that saves the given String in a file.
   * This is usefull to store xml configurations for debugging as they are quite big.
   * 
   * @param s The string to save to the file.
   * @param fileName The name of the file where stuff will be saved to.
   * 
   * @throws IOException
   */
  public static void writeToFile(final String s, final String fileName)  {

    try {

      FileWriter fstream = new FileWriter("out" + fileName);
      BufferedWriter out = new BufferedWriter(fstream);
      out.write(s);
      //Close the output stream
      out.close();

    } catch (Exception e) {
    }
  }
}
