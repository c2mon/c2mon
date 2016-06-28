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
package cern.c2mon.daq.tools;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
/**
 * Helper class to get the StrackTrace as a String.
 * 
 * @author Andreas Lang
 *
 */
public final class StackTraceHelper {
    
    /**
     * Static only class - there should be no instances.
     */
    private StackTraceHelper() {
    }

    /**
     * Creates the stack trace String from a throwable.
     * @param throwable The throwable to extract the stack trace.
     * @return The stack trace as String.
     */
    public static String getStackTrace(final Throwable throwable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        throwable.printStackTrace(printWriter);
        return result.toString();
    }

}
