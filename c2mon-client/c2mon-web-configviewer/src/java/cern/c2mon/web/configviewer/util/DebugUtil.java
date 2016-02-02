/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
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
